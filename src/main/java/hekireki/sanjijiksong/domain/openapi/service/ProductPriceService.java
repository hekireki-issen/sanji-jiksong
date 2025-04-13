package hekireki.sanjijiksong.domain.openapi.service;

import hekireki.sanjijiksong.domain.openapi.Repository.PriceDailyRepository;
import hekireki.sanjijiksong.domain.openapi.dto.PriceHistory;
import hekireki.sanjijiksong.domain.openapi.dto.PriceInfo;
import hekireki.sanjijiksong.domain.openapi.dto.ProductPriceResponse;
import hekireki.sanjijiksong.domain.openapi.entity.PriceDaily;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductPriceService {
    private final PriceDailyRepository priceDailyRepository;

    /**
     * 상품 가격 정보를 조회하는 메소드
     * fallback 계산을 위해 요청기간과는 별도로 extended 기간([extendedStart, endDay])의 데이터도 함께 조회합니다.
     *
     * @param startDay    조회 시작일
     * @param endDay      조회 종료일
     * @param categoryCode 카테고리 코드
     * @param itemCode    상품 코드
     * @return 상품 가격 정보 리스트
     */
    public List<ProductPriceResponse> getPriceInfo(LocalDate startDay, LocalDate endDay,
                                                   String categoryCode, String itemCode) {
        // extendedStart는 요청기간이 한 달 이상일 경우 요청기간 그대로 사용,
        // 그렇지 않으면 endDay 기준 한 달 전으로 확장하여 fallback 데이터 확보
        LocalDate extendedStart = (startDay.compareTo(endDay.minusMonths(1)) <= 0)
                ? startDay
                : endDay.minusMonths(1);

        // extended 범위의 데이터를 조회 (fallback 계산용)
        List<PriceDaily> fullList = priceDailyRepository.findByCategoryCodeAndItemCodeAndSnapshotDateBetween(
                categoryCode, itemCode, extendedStart, endDay);

        // 요청기간 내의 history용 데이터 필터링
        List<PriceDaily> historyList = fullList.stream()
                .filter(pd -> !pd.getSnapshotDate().isBefore(startDay) && !pd.getSnapshotDate().isAfter(endDay))
                .collect(Collectors.toList());

        // unit과 kindName 조합을 키로 그룹핑
        Map<PriceGroupKey, List<PriceDaily>> grouped = fullList.stream()
                .collect(Collectors.groupingBy(pd -> new PriceGroupKey(pd.getUnit(), pd.getKindName())));

        List<ProductPriceResponse> responses = new ArrayList<>();

        for (Map.Entry<PriceGroupKey, List<PriceDaily>> entry : grouped.entrySet()) {
            PriceGroupKey key = entry.getKey();
            List<PriceDaily> groupList = entry.getValue();
            // snapshotDate 오름차순 정렬
            groupList.sort(Comparator.comparing(PriceDaily::getSnapshotDate));

            // extended 데이터(fullList)에서 fallback 계산
            PriceDaily currentRecord = getLatestRecord(groupList, endDay);
            Integer currentPrice = (currentRecord != null) ? currentRecord.getPrice() : null;
            PriceDaily oneDayAgoRecord = getRecordOrFallback(groupList, endDay.minusDays(1));
            Integer oneDayAgoPrice = (oneDayAgoRecord != null) ? oneDayAgoRecord.getPrice() : null;
            PriceDaily oneWeekAgoRecord = getRecordOrFallback(groupList, endDay.minusWeeks(1));
            Integer oneWeekAgoPrice = (oneWeekAgoRecord != null) ? oneWeekAgoRecord.getPrice() : null;
            PriceDaily oneMonthAgoRecord = getRecordOrFallback(groupList, endDay.minusMonths(1));
            Integer oneMonthAgoPrice = (oneMonthAgoRecord != null) ? oneMonthAgoRecord.getPrice() : null;

            // currentRecord의 itemName 사용 (ex. "땅콩")
            String itemName = (currentRecord != null) ? currentRecord.getItemName() : "";

            // PriceInfo DTO 생성 (kindName 필드 포함)
            PriceInfo info = PriceInfo.builder()
                    .categoryCode(categoryCode)
                    .itemCode(itemCode)
                    .itemName(itemName)
                    .kindName(key.getKindName())
                    .unit(key.getUnit())
                    .currentPrice(currentPrice)
                    .oneDayAgoPrice(oneDayAgoPrice)
                    .oneWeekAgoPrice(oneWeekAgoPrice)
                    .oneMonthAgoPrice(oneMonthAgoPrice)
                    .startDate(startDay.toString())
                    .endDate(endDay.toString())
                    .build();

            // history 목록은 요청한 기간 내 데이터만 사용 (필요시 historyList에서 필터)
            List<PriceDaily> groupHistory = historyList.stream()
                    .filter(pd -> pd.getUnit().equals(key.getUnit()) && pd.getKindName().equals(key.getKindName()))
                    .collect(Collectors.toList());

            List<PriceHistory> history = groupHistory.stream()
                    .sorted(Comparator.comparing(PriceDaily::getSnapshotDate))
                    .map(pd -> PriceHistory.builder()
                            .date(pd.getSnapshotDate().toString())
                            .price(pd.getPrice())
                            .build())
                    .collect(Collectors.toList());

            responses.add(ProductPriceResponse.builder()
                    .info(info)
                    .history(history)
                    .build());
        }
        return responses;
    }

    /**
     * Helper 메소드: 주어진 그룹에서 targetDate 이하의 레코드 중 snapshotDate가 가장 최신인 레코드를 반환.
     * (리스트는 snapshotDate 오름차순으로 정렬되었다고 가정)
     */
    private PriceDaily getLatestRecord(List<PriceDaily> list, LocalDate targetDate) {
        PriceDaily result = null;
        for (PriceDaily pd : list) {
            if (!pd.getSnapshotDate().isAfter(targetDate)) {
                result = pd;
            } else {
                break;
            }
        }
        return result;
    }

    /**
     * 주어진 targetDate 이전에 해당하는 데이터가 없으면, 그룹 내 가장 오래된 데이터를 fallback으로 반환하는 메소드.
     */
    private PriceDaily getRecordOrFallback(List<PriceDaily> list, LocalDate targetDate) {
        PriceDaily record = getLatestRecord(list, targetDate);
        if (record == null && !list.isEmpty()) {
            record = list.get(0);
        }
        return record;
    }

    /**
     * 내부적으로 그룹핑 시 사용할 키 (unit + kindName)
     */
    private static class PriceGroupKey {
        private final String unit;
        private final String kindName;

        public PriceGroupKey(String unit, String kindName) {
            this.unit = unit;
            this.kindName = kindName;
        }

        public String getUnit() {
            return unit;
        }

        public String getKindName() {
            return kindName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PriceGroupKey)) return false;
            PriceGroupKey that = (PriceGroupKey) o;
            return Objects.equals(unit, that.unit) && Objects.equals(kindName, that.kindName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(unit, kindName);
        }
    }
}
