package hekireki.sanjijiksong.domain.openapi.service;

import hekireki.sanjijiksong.domain.openapi.Repository.PriceDailyRepository;
import hekireki.sanjijiksong.domain.openapi.Repository.TrendingKeywordRepository;
import hekireki.sanjijiksong.domain.openapi.dto.TrendingKeywordPrice;
import hekireki.sanjijiksong.domain.openapi.entity.PriceDaily;
import hekireki.sanjijiksong.domain.openapi.service.webdriver.WebDriverProvider;
import hekireki.sanjijiksong.domain.price.entity.TrendingKeyword;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrendingKeywordServiceTest {

    @Mock
    private TrendingKeywordRepository trendingKeywordRepository;

    @Mock
    private PriceDailyRepository priceDailyRepository;

    @Mock
    private WebDriverProvider webDriverProvider; // 목 주입

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WebDriver mockDriver; // 목 WebDriver

    @InjectMocks
    private TrendingKeywordService trendingKeywordService;

    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();
    }

    /**
     * 오늘 날짜의 TrendingKeyword가 있고, PriceDailyRepository에서 해당 keyword와 매칭되는 PriceDaily가 있을 때
     * TrendingKeywordPrice DTO가 생성되어 반환되는지 검증한다.
     */

    @Nested
    @DisplayName("getTrendingKeywordsPriceInfo 메서드 테스트")
    class getTrendingKeywordsPriceInfoTests{
        @Test
        @DisplayName("인기검색어로 실시간 가격 정보 조회에 성공한다.")
        public void testGetTrendingKeywordsPriceInfo_withMatchingPriceDaily() {
            // given
            String keyword = "한우";
            String category = "축산물";
            int rank = 1;
            TrendingKeyword trendingKeyword = TrendingKeyword.builder()
                    .keyword(keyword)
                    .category(category)
                    .rank(rank)
                    .createDate(today)
                    .build();
            List<TrendingKeyword> trendingKeywords = List.of(trendingKeyword);

            // trendingKeywordRepository는 오늘 날짜의 TrendingKeyword 목록을 반환
            when(trendingKeywordRepository.findByCreateDate(eq(today))).thenReturn(trendingKeywords);

            // PriceDailyRepository에서 '한우'를 포함하는 itemName의 최신 데이터를 반환
            PriceDaily priceDaily = PriceDaily.builder()
                    .itemName("한우등심")
                    .price(50000)
                    .snapshotDate(LocalDate.of(2025, 3, 7))
                    .build();
            when(priceDailyRepository.findTopByItemNameContainingOrderBySnapshotDateDesc(eq(keyword)))
                    .thenReturn(priceDaily);

            // when
            Map<String, TrendingKeywordPrice> result = trendingKeywordService.getTrendingKeywordsPriceInfo();

            // then
            assertNotNull(result, "반환 맵은 null이 아니어야 합니다.");
            assertTrue(result.containsKey(keyword), "반환 맵에 해당 키워드가 포함되어야 합니다.");

            TrendingKeywordPrice dto = result.get(keyword);
            assertEquals(keyword, dto.keyword());
            assertEquals(category, dto.category());
            assertEquals(rank, dto.trendingRank());
            assertEquals(today, dto.trendingCreateDate());
            // PriceDaily 관련 정보 검증
            assertEquals("한우등심", dto.itemName());
            assertEquals(50000, dto.price());
            assertEquals(LocalDate.of(2025, 3, 7), dto.snapshotDate());
        }

        /**
         * 오늘 날짜의 TrendingKeyword가 있으나, PriceDailyRepository에서 매칭되는 PriceDaily가 없을 경우
         * 해당 키워드는 결과 맵에 포함되지 않아야 한다.
         */
        @Test
        @DisplayName("인기검색어에 대한 실시간 가격 정보가 없을 경우, 결과 맵에 포함되지 않는다.")
        public void testGetTrendingKeywordsPriceInfo_withNoMatchingPriceDaily() {
            // given
            String keyword = "한우";
            TrendingKeyword trendingKeyword = TrendingKeyword.builder()
                    .keyword(keyword)
                    .category("축산물")
                    .rank(1)
                    .createDate(today)
                    .build();
            List<TrendingKeyword> trendingKeywords = List.of(trendingKeyword);
            when(trendingKeywordRepository.findByCreateDate(eq(today))).thenReturn(trendingKeywords);

            // PriceDailyRepository가 매칭되는 데이터를 반환하지 않음
            when(priceDailyRepository.findTopByItemNameContainingOrderBySnapshotDateDesc(eq(keyword)))
                    .thenReturn(null);

            // when
            Map<String, TrendingKeywordPrice> result = trendingKeywordService.getTrendingKeywordsPriceInfo();

            // then
            // 해당 trendingKeyword에 대해 매칭 PriceDaily가 없으므로 결과 맵에 포함되지 않아야 함
            assertNotNull(result, "반환 맵은 null이 아니어야 합니다.");
            assertFalse(result.containsKey(keyword), "매칭 PriceDaily가 없으므로 키워드가 결과 맵에 포함되어서는 안 됩니다.");
        }
    }

    @Test
    public void testSaveTodayTrendingKeywords() {
        // WebDriverProvider가 목 WebDriver를 반환하도록 설정
        when(webDriverProvider.getDriver()).thenReturn(mockDriver);

        // deep stubbing을 사용하므로, 별도의 manage() stub 처리 없이 체인 호출이 동작함
        doNothing().when(mockDriver).get(anyString());

        // saveTodayTrendingKeywords() 호출
        trendingKeywordService.saveTodayTrendingKeywords();

        // 최종적으로 driver.quit()이 호출되었는지 검증
        verify(mockDriver).quit();
    }
}
