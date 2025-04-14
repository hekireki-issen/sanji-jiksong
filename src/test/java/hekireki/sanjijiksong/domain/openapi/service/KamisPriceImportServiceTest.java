package hekireki.sanjijiksong.domain.openapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import hekireki.sanjijiksong.domain.openapi.Repository.PriceDailyRepository;
import hekireki.sanjijiksong.domain.openapi.dto.kamisDailyPrice.KamisDailyResponse;
import hekireki.sanjijiksong.domain.openapi.dto.kamisDailyPrice.KamisDailyResponse.Condition;
import hekireki.sanjijiksong.domain.openapi.dto.kamisDailyPrice.KamisDailyResponse.Data;
import hekireki.sanjijiksong.domain.openapi.dto.kamisDailyPrice.KamisDailyResponse.Item;
import hekireki.sanjijiksong.domain.openapi.dto.kamisDailyPrice.PriceDeserializer;
import hekireki.sanjijiksong.domain.openapi.entity.PriceDaily;
import hekireki.sanjijiksong.global.common.exception.KamisException;
import org.junit.jupiter.api.*;
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

import static org.junit.jupiter.api.Assertions.*;
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

    @Nested
    @DisplayName("KamisDailyResponse.from() 및 safeNormalize 테스트")
    class KamisDailyResponseFromTests {

        @Test
        @DisplayName("유효한 dpr1 값이 있는 Item은 PriceDaily로 변환된다.")
        public void testFrom_validItem() {
            // 조건 정보: regday, categoryCode, productClsCode 등 (dummy 값 사용)
            Condition condition = new Condition("01", Arrays.asList("KR"), "2025-03-07", "N", "100", "dummyKey", "dummyCertId", "json");
            // 두 개의 Item 중 하나는 정상적인 dpr1("55,318"), 다른 하나는 "-" 처리되어 필터링됨.
            Item validItem = new Item("쌀", "111", "일반", "01", "04", "상품", "kg", "55,318");
            Item invalidItem = new Item("쌀", "111", "일반", "01", "04", "상품", "kg", "-");
            Data data = new Data("000", Arrays.asList(validItem, invalidItem));
            KamisDailyResponse response = new KamisDailyResponse(Arrays.asList(condition), data);

            List<PriceDaily> result = response.from();
            // invalidItem은 safeNormalize 결과 null이 되어 필터링되므로, 결과는 1건이어야 함.
            assertEquals(1, result.size(), "유효한 아이템만 PriceDaily로 변환되어야 합니다.");

            PriceDaily pd = result.get(0);
            // "55,318" -> "55318" → 정수 55318
            assertEquals(55318, pd.getPrice(), "가격이 올바르게 파싱되어야 합니다.");
            assertEquals("쌀", pd.getItemName(), "itemName이 전달되어야 합니다.");
            // condition 정보를 통해 snapshotDate, categoryCode, classCode가 설정됨
            assertEquals(LocalDate.parse("2025-03-07"), pd.getSnapshotDate(), "snapshotDate가 조건의 regday로 설정되어야 합니다.");
            assertEquals("100", pd.getCategoryCode(), "categoryCode가 조건에서 전달되어야 합니다.");
            assertEquals("01", pd.getClassCode(), "classCode가 condition의 productClsCode로 설정되어야 합니다.");
        }

        @Test
        @DisplayName("data 또는 item이 null인 경우 예외를 발생시킨다.")
        public void testFrom_invalidData() {
            // 데이터가 null인 경우 RuntimeException 발생
            KamisDailyResponse response1 = new KamisDailyResponse(Arrays.asList(), null);
            Exception ex1 = assertThrows(RuntimeException.class, response1::from);
            assertEquals("잘못된 응답 형식입니다.", ex1.getMessage());

            // data.item()이 null인 경우도 마찬가지
            Data data = new Data("000", null);
            KamisDailyResponse response2 = new KamisDailyResponse(Arrays.asList(), data);
            Exception ex2 = assertThrows(RuntimeException.class, response2::from);
            assertEquals("잘못된 응답 형식입니다.", ex2.getMessage());
        }
    }

    @Nested
    @DisplayName("PriceDeserializer 테스트")
    class PriceDeserializerTests {
        private final ObjectMapper mapper = new ObjectMapper();

        @Test
        @DisplayName("data 필드가 배열로 온 경우 errorCode에 따른 예외가 발생한다 (001)")
        public void testDeserialize_errorCode001() {
            // 모듈 등록
            SimpleModule module = new SimpleModule();
            module.addDeserializer(KamisDailyResponse.Data.class, new PriceDeserializer());
            mapper.registerModule(module);

            String json = "[\"001\"]";
            Exception ex = assertThrows(KamisException.KamisApiNoDataException.class, () -> {
                mapper.readValue(json, KamisDailyResponse.Data.class);
            });
            assertNotNull(ex);
        }

        @Test
        @DisplayName("data 필드가 배열로 온 경우 errorCode에 따른 예외가 발생한다 (200)")
        public void testDeserialize_errorCode200() {
            SimpleModule module = new SimpleModule();
            module.addDeserializer(KamisDailyResponse.Data.class, new PriceDeserializer());
            mapper.registerModule(module);

            String json = "[\"200\"]";
            Exception ex = assertThrows(KamisException.KamisApiWrongParameterException.class, () -> {
                mapper.readValue(json, KamisDailyResponse.Data.class);
            });
            assertNotNull(ex);
        }

        @Test
        @DisplayName("data 필드가 배열로 온 경우 errorCode에 따른 예외가 발생한다 (900)")
        public void testDeserialize_errorCode900() {
            SimpleModule module = new SimpleModule();
            module.addDeserializer(KamisDailyResponse.Data.class, new PriceDeserializer());
            mapper.registerModule(module);

            String json = "[\"900\"]";
            Exception ex = assertThrows(KamisException.KamisApiUnauthenticatedException.class, () -> {
                mapper.readValue(json, KamisDailyResponse.Data.class);
            });
            assertNotNull(ex);
        }

        @Test
        @DisplayName("전체 JSON에서 data 필드가 객체로 온 경우 정상적으로 역직렬화된다.")
        public void testDeserialize_object() throws Exception {
            // 모듈 등록
            SimpleModule module = new SimpleModule();
            module.addDeserializer(KamisDailyResponse.Data.class, new PriceDeserializer());
            mapper.registerModule(module);

            String json = "{\n" +
                    "  \"condition\": [\n" +
                    "    {\n" +
                    "      \"p_product_cls_code\": \"01\",\n" +
                    "      \"p_country_code\": [],\n" +
                    "      \"p_regday\": \"2025-04-05\",\n" +
                    "      \"p_convert_kg_yn\": \"N\",\n" +
                    "      \"p_category_code\": \"100\",\n" +
                    "      \"p_cert_key\": \"269250d6-0396-4735-b42c-892ec5e81a89\",\n" +
                    "      \"p_cert_id\": \"5454\",\n" +
                    "      \"p_returntype\": \"json\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"data\": {\n" +
                    "    \"error_code\": \"000\",\n" +
                    "    \"item\": [\n" +
                    "      {\n" +
                    "        \"item_name\": \"쌀\",\n" +
                    "        \"item_code\": \"111\",\n" +
                    "        \"kind_name\": \"20kg(20kg)\",\n" +
                    "        \"kind_code\": \"01\",\n" +
                    "        \"rank\": \"상품\",\n" +
                    "        \"rank_code\": \"04\",\n" +
                    "        \"unit\": \"20kg\",\n" +
                    "        \"day1\": \"당일 (04/05)\",\n" +
                    "        \"dpr1\": \"55,318\",\n" +   // 유효한 값으로 변경
                    "        \"day2\": \"1일전 (04/04)\",\n" +
                    "        \"dpr2\": \"53,806\",\n" +
                    "        \"day3\": \"1주일전 (03/29)\",\n" +
                    "        \"dpr3\": \"-\",\n" +
                    "        \"day4\": \"2주일전 (03/22)\",\n" +
                    "        \"dpr4\": \"-\",\n" +
                    "        \"day5\": \"1개월전\",\n" +
                    "        \"dpr5\": \"41,751\",\n" +
                    "        \"day6\": \"1년전\",\n" +
                    "        \"dpr6\": \"49,681\",\n" +
                    "        \"day7\": \"일평년\",\n" +
                    "        \"dpr7\": \"51,716\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  }\n" +
                    "}";
            // 전체 JSON을 KamisDailyResponse로 역직렬화
            KamisDailyResponse response = mapper.readValue(json, KamisDailyResponse.class);
            assertNotNull(response, "역직렬화된 KamisDailyResponse는 null이 아니어야 합니다.");

            KamisDailyResponse.Data data = response.data();
            assertNotNull(data, "역직렬화된 Data 객체는 null이 아니어야 합니다.");
            assertEquals("000", data.errorCode());
            assertNotNull(data.item());
            assertEquals(1, data.item().size());
            KamisDailyResponse.Item item = data.item().get(0);
            assertEquals("쌀", item.itemName());
            assertEquals("55,318", item.dpr1());
        }
    }

}