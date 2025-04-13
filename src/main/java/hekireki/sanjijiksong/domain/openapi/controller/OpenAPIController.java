package hekireki.sanjijiksong.domain.openapi.controller;

import hekireki.sanjijiksong.domain.openapi.dto.ProductPriceResponse;
import hekireki.sanjijiksong.domain.openapi.dto.TrendingKeywordPrice;
import hekireki.sanjijiksong.domain.openapi.service.KamisPriceImportService;
import hekireki.sanjijiksong.domain.openapi.service.ProductPriceService;
import hekireki.sanjijiksong.domain.openapi.service.OpenApiScheduler;
import hekireki.sanjijiksong.domain.openapi.service.TrendingKeywordService;
import hekireki.sanjijiksong.global.common.exception.KamisException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/openapi")
@Validated
public class OpenAPIController {
    private final KamisPriceImportService kamisPriceImportService;
    private final ProductPriceService productPriceService;
    private final OpenApiScheduler openApiScheduler;
    private final TrendingKeywordService trendingKeywordService;


    // KAMIS API를 통해 가격 정보를 가져와 저장
    @GetMapping("/kamis/prices")
    public ResponseEntity<?> getPrice(@RequestParam(name = "category_code") String categoryCode,
                                      @RequestParam(name = "regday") String regDay) {
        kamisPriceImportService.getPrices(categoryCode, regDay);
        return ResponseEntity.ok("Price data fetched successfully");
    }

    @GetMapping("/kamis/allprices")
    public ResponseEntity<?> getAllPrice(@RequestParam(name = "start_day") String startDay,
                                         @RequestParam(name = "end_day") String endDay) {
        LocalDate start = LocalDate.parse(startDay);
        LocalDate end = LocalDate.parse(endDay);
        kamisPriceImportService.getAllPricesBetween(start,end);
        return ResponseEntity.ok("All price data fetched successfully");
    }

    @GetMapping("/getPrices")
    public ResponseEntity<?> getPrices(
            @RequestParam("item_code") @NotBlank String itemCode,
            @RequestParam("category_code") @NotBlank String categoryCode,
            @RequestParam("start_date") @NotBlank String startDate,
            @RequestParam("end_date") @NotBlank String endDate) {

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        long dayDiffrence = ChronoUnit.DAYS.between(start, end);
        if (dayDiffrence > 30) {
            throw new KamisException.PriceQueryPeriodTooLongException();
        }

        List<ProductPriceResponse> response = productPriceService.getPriceInfo(start, end, categoryCode, itemCode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/naver/crawling")
    public ResponseEntity<?> getCrawler(){
        trendingKeywordService.saveTodayTrendingKeywords();
        return ResponseEntity.ok("Crawling completed successfully");
    }

    @GetMapping("/naver/trending")
    public ResponseEntity<?> getTrendingKeywords() {
        Map<String, TrendingKeywordPrice> priceInfoForTrendingKeywords = trendingKeywordService.getTrendingKeywordsPriceInfo();
        return ResponseEntity.ok(priceInfoForTrendingKeywords);
    }
}
