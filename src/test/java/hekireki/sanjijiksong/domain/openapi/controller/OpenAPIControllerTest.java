package hekireki.sanjijiksong.domain.openapi.controller;

import hekireki.sanjijiksong.domain.openapi.dto.PriceInfo;
import hekireki.sanjijiksong.domain.openapi.dto.ProductPriceResponse;
import hekireki.sanjijiksong.domain.openapi.service.KamisPriceImportService;
import hekireki.sanjijiksong.domain.openapi.service.OpenApiScheduler;
import hekireki.sanjijiksong.domain.openapi.service.ProductPriceService;
import hekireki.sanjijiksong.domain.openapi.service.TrendingKeywordService;
import hekireki.sanjijiksong.global.common.exception.KamisException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OpenAPIControllerTest {
    @Mock
    private KamisPriceImportService kamisPriceImportService;
    @Mock
    private ProductPriceService productPriceService;
    @Mock
    private OpenApiScheduler openApiScheduler;
    @Mock
    private TrendingKeywordService trendingKeywordService;

    @InjectMocks
    private OpenAPIController openAPIController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(openAPIController)
                .build();
    }


    @Nested
    @DisplayName("getPrice 테스트")
    class getPriceTests{
        private static final String BASE_URL = "/api/v1/openapi/kamis/prices";
        @Test
        @DisplayName("정상적인 조건으로 가격을 가져오는 데 성공한다.")
        void success() throws Exception {

            //when
            ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.get(BASE_URL)
                            .param("category_code", "100")
                            .param("regday", "2025-03-07")
            );
            //then
            resultActions.andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("getAllPrice 테스트")
    class getAllPriceTests{
        private static final String BASE_URL = "/api/v1/openapi/kamis/allprices";
        @Test
        @DisplayName("getAllPrice 정상 테스트")
        void getAllPrice_success() throws Exception {
            // given
            String startDay = "2025-03-01";
            String endDay = "2025-03-07";

            // when
            ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.get(BASE_URL)
                            .param("start_day", startDay)
                            .param("end_day", endDay)
            );
            // then
            resultActions.andExpect(status().isOk())
                    .andExpect(jsonPath("$").value("All price data fetched successfully"));

            // 서비스 호출 검증
            Mockito.verify(kamisPriceImportService).getAllPricesBetween(
                    LocalDate.parse(startDay), LocalDate.parse(endDay)
            );
        }
    }

    @Nested
    @DisplayName("getPrices 테스트")
    class getPricesTests {
        private static final String BASE_URL = "/api/v1/openapi/getPrices";

        @Test
        @DisplayName("날짜 기간이 30일 이하일 경우 가격 정보를 가져온다.")
        void success() throws Exception {
            // given
            String itemCode = "ITEM1";
            String categoryCode = "CAT1";
            String startDate = "2025-03-01";
            String endDate = "2025-03-07";

            List<ProductPriceResponse> dummyResponse = new ArrayList<>();
            // 필요에 따라 dummyResponse에 데이터를 추가할 수 있음.
            Mockito.when(productPriceService.getPriceInfo(
                    LocalDate.parse(startDate),
                    LocalDate.parse(endDate),
                    categoryCode,
                    itemCode
            )).thenReturn(dummyResponse);

            // when
            ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.get(BASE_URL)
                            .param("item_code", itemCode)
                            .param("category_code", categoryCode)
                            .param("start_date", startDate)
                            .param("end_date", endDate)
            );
            // then
            resultActions.andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("날짜 기간이 30일 초과일 경우 예외를 발생시킨다.")
        void fail_dueToPeriodTooLong() throws Exception {
            // given
            String itemCode = "ITEM1";
            String categoryCode = "CAT1";
            // 40일 차이 (예: 2025-01-01 ~ 2025-02-10)
            String startDate = "2025-01-01";
            String endDate = "2025-02-10";

            // when
            Exception exception = assertThrows(Exception.class, () -> {
                mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/openapi/getPrices")
                                .param("item_code", itemCode)
                                .param("category_code", categoryCode)
                                .param("start_date", startDate)
                                .param("end_date", endDate)
                ).andReturn();
            });
            // then: 예외가 발생하여 500 에러가 발생한다.
            Throwable ex = exception.getCause();
            while (ex instanceof jakarta.servlet.ServletException && ex.getCause() != null) {
                ex = ex.getCause();
            }
            assertTrue(ex instanceof KamisException.PriceQueryPeriodTooLongException);

            // 서비스 메서드가 호출되지 않았는지 검증
            Mockito.verify(productPriceService, Mockito.never()).getPriceInfo(
                    Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.anyString()
            );
        }
    }
    @Test
    @DisplayName("getCrawler 테스트")
    void getCrawler_success() throws Exception {
        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/openapi/naver/crawling")
        );
        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Crawling completed successfully"));

        // trendingKeywordService의 메서드 호출 여부 검증
        Mockito.verify(trendingKeywordService).saveTodayTrendingKeywords();
    }

    @Test
    @DisplayName("getTrending 테스트")
    void getTrending_success() throws Exception {
        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/openapi/naver/trending")
        );
        // then
        resultActions.andExpect(status().isOk());

        // trendingKeywordService의 메서드 호출 여부 검증
        Mockito.verify(trendingKeywordService).getTrendingKeywordsPriceInfo();
    }
}