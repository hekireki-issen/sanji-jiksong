package hekireki.sanjijiksong.domain.price.controller;


import hekireki.sanjijiksong.domain.price.dto.ProductPriceResponse;
import hekireki.sanjijiksong.domain.price.service.PriceService;
import hekireki.sanjijiksong.domain.price.service.ProductPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PriceController {
    private final PriceService priceService;
    private final ProductPriceService productPriceService;


    // 가격 정보를 가져오는 API 엔드포인트
    @GetMapping("/api/v1/test/price")
    public ResponseEntity<?> getPrice(@RequestParam(name = "category_code") String categoryCode,
                                      @RequestParam(name = "regday") String regDay) {
        priceService.getPrice(categoryCode, regDay);
        return ResponseEntity.ok("Price data fetched successfully");
    }

    @GetMapping("/api/v1/test/getAllPrice")
    public ResponseEntity<?> getAllPrice() {
        priceService.getAllPricesForPastYear();
        return ResponseEntity.ok("All price data fetched successfully");
    }

    @GetMapping("/api/v1/getPrices")
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
}
