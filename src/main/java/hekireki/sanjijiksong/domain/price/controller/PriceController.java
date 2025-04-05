package hekireki.sanjijiksong.domain.price.controller;


import hekireki.sanjijiksong.domain.price.service.PriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PriceController {
    private final PriceService priceService;


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

}
