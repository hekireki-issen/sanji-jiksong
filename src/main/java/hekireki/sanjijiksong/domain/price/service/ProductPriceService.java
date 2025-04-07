package hekireki.sanjijiksong.domain.price.service;

import hekireki.sanjijiksong.domain.price.Repository.PriceDailyRepository;
import hekireki.sanjijiksong.domain.price.dto.PriceHistory;
import hekireki.sanjijiksong.domain.price.dto.PriceInfo;
import hekireki.sanjijiksong.domain.price.dto.ProductPriceResponse;
import hekireki.sanjijiksong.domain.price.entity.PriceDaily;
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
     *
     * @param startDay    조회 시작일 (yyyy-MM-dd 형식)
     * @param endDay      조회 종료일 (yyyy-MM-dd 형식)
     * @param categoryCode 카테고리 코드
     * @param itemCode    상품 코드
     * @return 상품 가격 정보 리스트
     */
    public List<ProductPriceResponse> getPriceInfo(String startDay, String endDay,
                                                   String categoryCode, String itemCode) {
        LocalDate start = LocalDate.parse(startDay);
        LocalDate end = LocalDate.parse(endDay);

        // 1. History 조회: 지정된 기간 내의 모든 데이터 조회
        List<PriceDaily> dailyList = priceDailyRepository.findByCategoryCodeAndItemCodeAndSnapshotDateBetween(
                categoryCode, itemCode, start, end);

        // 2. unit별로 그룹화
        Map<String, List<PriceDaily>> groupedByUnit = dailyList.stream()
                .collect(Collectors.groupingBy(PriceDaily::getUnit));

        List<ProductPriceResponse> responses = new ArrayList<>();

        // 3. 각 그룹별로 info와 history를 구성
        for (Map.Entry<String, List<PriceDaily>> entry : groupedByUnit.entrySet()) {
            String unit = entry.getKey();
            List<PriceDaily> groupList = entry.getValue();
            // 정렬: snapshotDate 오름차순 정렬
            groupList.sort(Comparator.comparing(PriceDaily::getSnapshotDate));

            // Info: 조회 종료일(end)을 기준으로 가격 정보 조회
            PriceDaily currentRecord = getLatestRecord(groupList, end);
            Integer currentPrice = currentRecord != null ? currentRecord.getPrice() : null;
            PriceDaily oneDayAgoRecord = getLatestRecord(groupList, end.minusDays(1));
            Integer oneDayAgoPrice = oneDayAgoRecord != null ? oneDayAgoRecord.getPrice() : null;
            PriceDaily oneWeekAgoRecord = getLatestRecord(groupList, end.minusWeeks(1));
            Integer oneWeekAgoPrice = oneWeekAgoRecord != null ? oneWeekAgoRecord.getPrice() : null;
            PriceDaily oneMonthAgoRecord = getLatestRecord(groupList, end.minusMonths(1));
            Integer oneMonthAgoPrice = oneMonthAgoRecord != null ? oneMonthAgoRecord.getPrice() : null;


            // 상품 기본 정보: currentRecord가 있으면 사용
            String itemName = currentRecord != null ? currentRecord.getItemName() : "";

            PriceInfo info = PriceInfo.builder()
                    .categoryCode(categoryCode)
                    .itemCode(itemCode)
                    .itemName(itemName)
                    .unit(unit)
                    .currentPrice(currentPrice)
                    .oneDayAgoPrice(oneDayAgoPrice)
                    .oneWeekAgoPrice(oneWeekAgoPrice)
                    .oneMonthAgoPrice(oneMonthAgoPrice)
                    .startDate(start.toString())
                    .endDate(end.toString())
                    .build();

            // History: 그룹 내의 모든 데이터를 PriceHistory로 변환
            List<PriceHistory> history = groupList.stream()
                    .map(pd -> PriceHistory.builder()
                            .date(pd.getSnapshotDate().toString())
                            .price(pd.getPrice())
                            .build())
                    .collect(Collectors.toList());

            ProductPriceResponse response = ProductPriceResponse.builder()
                    .info(info)
                    .history(history)
                    .build();

            responses.add(response);
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
}