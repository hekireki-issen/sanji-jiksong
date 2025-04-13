package hekireki.sanjijiksong.domain.openapi.service;


import hekireki.sanjijiksong.domain.openapi.Repository.PriceDailyRepository;
import hekireki.sanjijiksong.domain.openapi.dto.kamisDailyPrice.KamisDailyResponse;
import hekireki.sanjijiksong.domain.openapi.entity.PriceDaily;
import hekireki.sanjijiksong.global.common.exception.KamisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;



/**
 * KAMIS API를 통해 가격 정보를 가져오는 서비스
 * @author sanjijiksong
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class KamisPriceImportService {
    @Value("${kamis.cert.id}")
    private String certId;
    @Value("${kamis.cert.key}")
    private String certKey;

    private static final String SEARCH_PRICE_URL = "http://www.kamis.co.kr/service/price/xml.do?action=dailyPriceByCategoryList";
    private static final List<String> CATEGORY_CODES = Arrays.asList("100", "200", "300", "400", "500", "600");

    private final PriceDailyRepository priceDailyRepository;
    private final RestTemplate restTemplate;

    /**
     * 특정 카테고리와 날짜에 대한 가격 정보를 가져오는 메서드
     * @param categoryCode 카테고리 코드
     * @param regDay 조회할 날짜 (yyyy-MM-dd 형식)
     */
    @Transactional
    public void getPrices(String categoryCode, String regDay) {
        URI targetUri = buildKamisUri(categoryCode, regDay);
        KamisDailyResponse response = restTemplate.exchange(targetUri, HttpMethod.GET, getHttpEntity(), KamisDailyResponse.class).getBody();

        List<PriceDaily> priceList = response.from();
        log.info("Price List: {}", priceList.toString());

        priceDailyRepository.saveAll(priceList);
    }

    /**
     * KAMIS API 호출을 위한 URI를 생성하는 메서드
     * @param categoryCode 카테고리 코드
     * @param regDay 조회할 날짜 (yyyy-MM-dd 형식)
     * @return 생성된 URI
     */
    private URI buildKamisUri(String categoryCode, String regDay) {
        return UriComponentsBuilder
                .fromUriString(SEARCH_PRICE_URL)
                .queryParam("p_cert_id", certId)
                .queryParam("p_cert_key", certKey)
                .queryParam("p_returntype", "json")
                .queryParam("p_product_cls_code", "01")
                .queryParam("p_item_category_code", categoryCode)
                .queryParam("p_regday", regDay)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri();
    }

    /**
     * 특정 날짜 범위에 대한 가격 정보를 가져오는 메서드
     * @param start 시작 날짜
     * @param end 종료 날짜
     */
    @Transactional
    public void getAllPricesBetween(LocalDate start, LocalDate end) {
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            String regDay = date.toString(); // "yyyy-MM-dd" 형식
            for (String categoryCode : CATEGORY_CODES) {
                try {
                    getPrices(categoryCode, regDay);
                } catch (Exception e) {
                    // 각 API 호출 시 발생한 예외 로깅 후 다음 카테고리 호출 진행
                    log.error("카테고리 {}의 {} 데이터 조회 실패: {}", categoryCode, regDay, e.getMessage());
                }
            }
        }
    }

    private HttpEntity<String> getHttpEntity() {
        // 헤더 인증 정보 추가
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", "application/json");
        return new HttpEntity<>(httpHeaders);
    }

}
