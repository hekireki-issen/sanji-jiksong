package hekireki.sanjijiksong.domain.price.service;


import hekireki.sanjijiksong.domain.price.Repository.PriceDailyRepository;
import hekireki.sanjijiksong.domain.price.dto.kamisDailyPrice.KamisDailyResponse;
import hekireki.sanjijiksong.domain.price.entity.PriceDaily;
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
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PriceService {
    @Value("${KAMIS_CERT_ID}")
    private String certId;
    @Value("${KAMIS_CERT_KEY}")
    private String certKey;

    private static final String SEARCH_PRICE_URL = "http://www.kamis.co.kr/service/price/xml.do?action=dailyPriceByCategoryList";


    private final PriceDailyRepository priceDailyRepository;
    private final RestTemplate restTemplate;

    @Transactional
    public void getPrice(String categoryCode, String regDay) {

        URI targetUri = UriComponentsBuilder
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
        ;

        KamisDailyResponse response = restTemplate.exchange(targetUri, HttpMethod.GET, getHttpEntity(), KamisDailyResponse.class).getBody();
        List<PriceDaily> priceList = response.from();
        log.info("Price List: {}", priceList.toString());


        // DB에 저장
        priceDailyRepository.saveAll(priceList);
    }

    @Transactional
    public void getAllPricesForPastYear(){
        LocalDate today = LocalDate.of(2025,3,2);
        //LocalDate oneMonthAgo = today.minusMonths(1);
        //2025-03-03
        LocalDate ago = LocalDate.of(2025, 1, 3);
        for (LocalDate date = ago; !date.isAfter(today); date = date.plusDays(1)) {
            String regDay = date.toString(); // "yyyy-MM-dd" 형식
            // 6개의 카테고리 코드에 대해 반복
            for (String categoryCode : new String[]{"100", "200", "300", "400", "500", "600"}) {
                try {
                    saveDailyPrice(categoryCode, regDay);
                } catch (Exception e) {
                    // 각 API 호출 시 발생한 예외 로깅 후 다음 카테고리 호출 진행
                    // 예: logger.warn("카테고리 {}의 {} 데이터 조회 실패: {}", categoryCode, regDay, e.getMessage());
                }
            }
        }
    }

    @Transactional
    public void saveDailyPrice(String categoryCode, String regDay){
        getPrice(categoryCode, regDay);
    }

    private HttpEntity<String> getHttpEntity() {
        // 헤더 인증 정보 추가
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", "application/json");
        return new HttpEntity<>(httpHeaders);
    }
}
