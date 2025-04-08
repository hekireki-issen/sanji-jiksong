package hekireki.sanjijiksong.domain.price.controller;


import hekireki.sanjijiksong.domain.price.dto.ProductPriceResponse;
import hekireki.sanjijiksong.domain.price.entity.PriceDaily;
import hekireki.sanjijiksong.domain.price.entity.TrendingKeywordPrice;
import hekireki.sanjijiksong.domain.price.service.PriceService;
import hekireki.sanjijiksong.domain.price.service.ProductPriceService;
import hekireki.sanjijiksong.domain.price.service.TrendingKeywordScheduler;
import hekireki.sanjijiksong.domain.price.service.TrendingKeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/openapi")
public class PriceController {
    private final PriceService priceService;
    private final ProductPriceService productPriceService;
    private final TrendingKeywordScheduler trendingKeywordScheduler;
    private final TrendingKeywordService trendingKeywordService;


    // 가격 정보를 가져오는 API 엔드포인트
    @GetMapping("/v1/test/price")
    public ResponseEntity<?> getPrice(@RequestParam(name = "category_code") String categoryCode,
                                      @RequestParam(name = "regday") String regDay) {
        priceService.getPrice(categoryCode, regDay);
        return ResponseEntity.ok("Price data fetched successfully");
    }

    @GetMapping("/v1/test/getAllPrice")
    public ResponseEntity<?> getAllPrice() {
        priceService.getAllPricesForPastYear();
        return ResponseEntity.ok("All price data fetched successfully");
    }

    @GetMapping("/v1/getPrices")
    public ResponseEntity<?> getPrices(
            @RequestParam("item_code") String itemCode,
            @RequestParam("category_code") String categoryCode,
            @RequestParam("start_date") String startDate,
            @RequestParam("end_date") String endDate) {

        LocalDate today = LocalDate.now();
        if(startDate == null || startDate.trim().isEmpty()){
            startDate = today.toString();
        }
        if(endDate == null || endDate.trim().isEmpty()){
            endDate = today.toString();
        }
        List<ProductPriceResponse> response = productPriceService.getPriceInfo(startDate, endDate, categoryCode, itemCode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/v1/crawler")
    public ResponseEntity<?> getCrawler(){
        trendingKeywordScheduler.collectTrendingKeywords();
        return ResponseEntity.ok("Crawling completed successfully");
    }

    @GetMapping("/v1/trending")
    public ResponseEntity<?> getTrendingKeywords() {
        Map<String, TrendingKeywordPrice> priceInfoForTrendingKeywords = trendingKeywordService.getLatestPriceInfoForTodayTrendingKeywords();
        return ResponseEntity.ok(priceInfoForTrendingKeywords);
    }
}
