package hekireki.sanjijiksong.domain.openapi.service;

import hekireki.sanjijiksong.domain.openapi.Repository.PriceDailyRepository;
import hekireki.sanjijiksong.domain.openapi.dto.PriceHistory;
import hekireki.sanjijiksong.domain.openapi.dto.PriceInfo;
import hekireki.sanjijiksong.domain.openapi.dto.ProductPriceResponse;
import hekireki.sanjijiksong.domain.openapi.entity.PriceDaily;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductPriceServiceTest {

    @Mock
    private PriceDailyRepository priceDailyRepository;

    @InjectMocks
    private ProductPriceService productPriceService;

    /**
     * 테스트에 사용할 PriceDaily 객체를 생성하는 헬퍼 메서드입니다.
     */
    private PriceDaily createPriceDaily(LocalDate snapshotDate, int price, String unit, String kindName, String itemName) {
        return PriceDaily.builder()
                .snapshotDate(snapshotDate)
                .price(price)
                .unit(unit)
                .kindName(kindName)
                .itemName(itemName)
                .build();
    }

    @Test
    public void testGetPriceInfo() {
        // given
        LocalDate startDay = LocalDate.of(2025, 3, 1);
        LocalDate endDay = LocalDate.of(2025, 3, 7);
        String categoryCode = "CAT1";
        String itemCode = "ITEM1";

        /*
         * extendedStart 계산:
         * endDay.minusMonths(1) = 2025-02-07
         * startDay (2025-03-01)가 2025-02-07보다 늦으므로 extendedStart는 2025-02-07
         */
        LocalDate extendedStart = endDay.minusMonths(1); // 2025-02-07

        // 해당 그룹(unit="KG", kindName="일반", itemName="땅콩")에 대하여 세 건의 데이터를 생성
        // extended 기간에 속하는 record1 (fallback 용)
        PriceDaily record1 = createPriceDaily(LocalDate.of(2025, 2, 10), 100, "KG", "일반", "땅콩");
        // 요청 기간 내에 속하는 record2
        PriceDaily record2 = createPriceDaily(LocalDate.of(2025, 3, 1), 110, "KG", "일반", "땅콩");
        // 요청 기간 내에 속하는 record3 (더 최신)
        PriceDaily record3 = createPriceDaily(LocalDate.of(2025, 3, 6), 120, "KG", "일반", "땅콩");

        // fullList는 extendedStart부터 endDay까지의 데이터를 포함합니다.
        List<PriceDaily> fullList = Arrays.asList(record1, record2, record3);

        // PriceDailyRepository의 동작을 목으로 설정합니다.
        when(priceDailyRepository.findByCategoryCodeAndItemCodeAndSnapshotDateBetween(
                eq(categoryCode), eq(itemCode), eq(extendedStart), eq(endDay)
        )).thenReturn(fullList);

        // when: getPriceInfo 메서드 호출
        List<ProductPriceResponse> responses = productPriceService.getPriceInfo(startDay, endDay, categoryCode, itemCode);

        // then
        assertNotNull(responses, "응답은 null이어서는 안 됩니다.");
        assertEquals(1, responses.size(), "하나의 그룹에 대한 응답이어야 합니다.");

        ProductPriceResponse response = responses.get(0);
        PriceInfo info = response.info();

        // info 검증
        assertEquals(categoryCode, info.categoryCode());
        assertEquals(itemCode, info.itemCode());
        assertEquals("땅콩", info.itemName());
        assertEquals("일반", info.kindName());
        assertEquals("KG", info.unit());
        assertEquals("2025-03-01", info.startDate());
        assertEquals("2025-03-07", info.endDate());

        // fallback 로직 검증
        // currentRecord: 2025-03-06 (record3) → 가격 120
        assertEquals(120, info.currentPrice());
        // oneDayAgo: target = 2025-03-06 → record3 (가격 120)
        assertEquals(120, info.oneDayAgoPrice());
        // oneWeekAgo: target = 2025-02-28 → record1 (가격 100)
        assertEquals(100, info.oneWeekAgoPrice());
        // oneMonthAgo: target = 2025-02-07, 해당 날짜 이하의 데이터 없으므로 fallback → record1 (가격 100)
        assertEquals(100, info.oneMonthAgoPrice());

        // history 목록 검증: 요청 기간 내(2025-03-01 ~ 2025-03-07)에는 record2와 record3가 포함되어야 함
        List<PriceHistory> history = response.history();
        assertNotNull(history, "history 목록은 null이어서는 안 됩니다.");
        assertEquals(2, history.size(), "history 목록은 2건이어야 합니다.");
        // 정렬 순서는 snapshotDate 오름차순이어야 하므로 record2(2025-03-01) 후 record3(2025-03-06)
        assertEquals("2025-03-01", history.get(0).date());
        assertEquals(110, history.get(0).price());
        assertEquals("2025-03-06", history.get(1).date());
        assertEquals(120, history.get(1).price());

        // 리포지토리 호출 검증
        verify(priceDailyRepository).findByCategoryCodeAndItemCodeAndSnapshotDateBetween(
                eq(categoryCode), eq(itemCode), eq(extendedStart), eq(endDay)
        );
    }
}
