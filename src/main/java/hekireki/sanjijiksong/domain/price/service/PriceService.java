package hekireki.sanjijiksong.domain.price.service;


import hekireki.sanjijiksong.domain.price.Repository.PriceDailyRepository;
import hekireki.sanjijiksong.domain.price.dto.KamisDailyResponse;
import hekireki.sanjijiksong.domain.price.entity.PriceDaily;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PriceService {
    @Value("${KAMIS_CERT_ID}")
    private String certId;
    @Value("${KAMIS_CERT_KEY}")
    private String certKey;

    //private static final String SEARCH_PRICE_URL = "http://www.kamis.co.kr/service/price/xml.do?action=dailySalesList";
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

//        String response = restTemplate.exchange(targetUri, HttpMethod.GET, null, String.class).getBody();
        KamisDailyResponse response = restTemplate.exchange(targetUri, HttpMethod.GET, getHttpEntity(), KamisDailyResponse.class).getBody();
        List<PriceDaily> priceList = response.from();

        ArrayList<PriceDaily> updatedList = new ArrayList<>();
        for (PriceDaily pd : priceList) {
            if(pd.getPrice() == null) {
                Optional<PriceDaily> previousOpt = priceDailyRepository.findTopByItemCodeAndRankCodeAndSnapshotDateLessThanOrderBySnapshotDateDesc(pd.getItemCode(), pd.getRankCode(), pd.getSnapshotDate());

                if(previousOpt.isPresent()) {
                    pd.updatePrice(previousOpt.get().getPrice());
                } else{
                    pd.updatePrice("0");
                }
            }
        }

        // DB에 저장
        priceDailyRepository.saveAll(priceList);
    }
    @Transactional
    public void getAllPricesForPastYear(){
        LocalDate today = LocalDate.now();
        LocalDate oneYearAgo = today.minusYears(1);
        for (LocalDate date = oneYearAgo; !date.isAfter(today); date = date.plusDays(1)) {
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
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
