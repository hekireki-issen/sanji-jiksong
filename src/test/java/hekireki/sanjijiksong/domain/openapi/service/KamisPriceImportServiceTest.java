package hekireki.sanjijiksong.domain.openapi.service;

import hekireki.sanjijiksong.domain.openapi.Repository.PriceDailyRepository;
import hekireki.sanjijiksong.domain.openapi.dto.kamisDailyPrice.KamisDailyResponse;
import hekireki.sanjijiksong.domain.openapi.dto.kamisDailyPrice.KamisDailyResponse.Condition;
import hekireki.sanjijiksong.domain.openapi.dto.kamisDailyPrice.KamisDailyResponse.Data;
import hekireki.sanjijiksong.domain.openapi.dto.kamisDailyPrice.KamisDailyResponse.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class KamisPriceImportServiceTest {
    @Mock
    private PriceDailyRepository priceDailyRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private KamisPriceImportService kamisPriceImportService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }



    @Nested
    @DisplayName("getPrices 테스트")
    class getPriceTests {

        @Test
        @DisplayName("정상적인 조건으로 가격을 가져오는 데 성공한다.")
        void success() {
            // given
            String categoryCode = "100";
            String regDay = "2025-03-07";
            // 정상적인 조건 정보를 만듭니다.
            Condition condition = new Condition("01", Arrays.asList("KR"), regDay, "N",
                    "100", "dummyCertKey", "5454", "json");
            // 정상적인 data 객체 생성 (item 리스트 포함)
            Item item1 = new Item("쌀", "111", "20kg(20kg)", "01", "04", "상품", "20kg", "55,318");
            Item item2 = new Item("쌀", "111", "10kg(10kg)", "10", "04", "상품", "10kg", "28,992");
            Data data = new Data("000", Arrays.asList(item1, item2));
            KamisDailyResponse response = new KamisDailyResponse(Arrays.asList(condition), data);

            // restTemplate.exchange(...) 가 정상 응답을 반환하도록 stub 설정
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
                            eq(KamisDailyResponse.class)))
                    .thenReturn(ResponseEntity.ok(response));

            // PriceDailyRepository.saveAll() 호출을 캡처
            when(priceDailyRepository.saveAll(any(List.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            kamisPriceImportService.getPrices(categoryCode, regDay);

            // then : PriceDailyRepository.saveAll()이 호출되었는지 검증
            verify(priceDailyRepository, times(1)).saveAll(any(List.class));
        }
    }

    @Nested
    @DisplayName("getAllPricesBetween 테스트")
    class getAllPricesBetweenTests {
        private static final List<String> CATEGORY_CODES = Arrays.asList("100", "200", "300", "400", "500", "600");

        @Test
        @DisplayName("주어진 날짜 범위에 대해 모든 가격을 가져오는데 성공한다.")
        void success() {
            // Given
            LocalDate start = LocalDate.of(2025, 3, 1);
            LocalDate end = LocalDate.of(2025, 3, 2); // 2일간 => 2 * 6 = 12번 호출

            // 정상 응답에 사용할 dummy KamisDailyResponse 생성
            // condition: regday가 테스트 날짜 중 하나로 설정 (테스트에서는 중요하지 않으므로 "2025-03-01" 사용)
            Condition condition = new Condition("01", Arrays.asList("KR"), "2025-03-01", "N", "100", "dummyKey", "5454", "json");
            // item: 하나의 PriceDaily 항목 (예: 쌀, 20kg)
            Item item = new Item("쌀", "111", "20kg(20kg)", "01", "04", "상품", "20kg", "55,318");
            Data data = new Data("000", Arrays.asList(item));
            KamisDailyResponse dummyResponse = new KamisDailyResponse(Arrays.asList(condition), data);

            // restTemplate.exchange(...)가 호출될 때마다 dummyResponse를 반환하도록 설정
            when(restTemplate.exchange(any(URI.class),
                    eq(HttpMethod.GET), any(HttpEntity.class), eq(KamisDailyResponse.class)))
                    .thenReturn(ResponseEntity.ok(dummyResponse));

            // priceDailyRepository.saveAll()는 호출되는 인자를 그대로 반환하도록 stub 설정
            when(priceDailyRepository.saveAll(any(List.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            kamisPriceImportService.getAllPricesBetween(start, end);

            // Then
            // 2일간 * 6 카테고리 = 12번 REST 호출이 있어야 한다.
            verify(restTemplate, times(12)).exchange(any(URI.class),
                    eq(HttpMethod.GET), any(HttpEntity.class), eq(KamisDailyResponse.class));
        }
    }


}