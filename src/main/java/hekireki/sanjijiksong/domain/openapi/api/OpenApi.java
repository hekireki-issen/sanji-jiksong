package hekireki.sanjijiksong.domain.openapi.api;

import hekireki.sanjijiksong.domain.openapi.dto.ProductPriceResponse;
import hekireki.sanjijiksong.domain.openapi.dto.TrendingKeywordPrice;
import hekireki.sanjijiksong.global.common.exception.KamisException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "OpenApi", description = "KAMIS 및 네이버 트렌딩 키워드 관련 API")
@RequestMapping("/api/v1/openapi")
public interface OpenApi {

    @Operation(summary = "KAMIS 가격 조회", description = "특정 카테고리와 날짜의 KAMIS 가격 정보를 조회하여 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상적으로 가격 정보를 조회 및 저장하였습니다.",
                    content = @Content(schema = @Schema(implementation = Void.class),
                            examples = @ExampleObject(value = "null")))
    })
    @GetMapping("/kamis/prices")
    ResponseEntity<?> getPrice(
            @RequestParam(name = "category_code")
            @Parameter(description = "카테고리 코드", example = "100")
            String categoryCode,
            @RequestParam(name = "regday")
            @Parameter(description = "조회 날짜 (yyyy-MM-dd)", example = "2025-03-07")
            String regDay);

    @Operation(summary = "전체 KAMIS 가격 조회", description = "시작일과 종료일 사이의 KAMIS 가격 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전체 가격 데이터를 성공적으로 조회하였습니다.",
                    content = @Content(schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "All price data fetched successfully")))
    })
    @GetMapping("/kamis/allprices")
    ResponseEntity<?> getAllPrice(
            @RequestParam(name = "start_day")
            @Parameter(description = "조회 시작일 (yyyy-MM-dd)", example = "2025-03-01")
            String startDay,
            @RequestParam(name = "end_day")
            @Parameter(description = "조회 종료일 (yyyy-MM-dd)", example = "2025-03-07")
            String endDay);

    @Operation(summary = "상품 가격 정보 조회", description = "상품 코드, 카테고리 코드, 시작일과 종료일을 기반으로 상품 가격 정보를 조회합니다. (조회 기간은 30일 이내)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 가격 정보 조회 성공.",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductPriceResponse.class)))),
            @ApiResponse(responseCode = "500", description = "조회 기간이 30일을 초과한 경우",
                    content = @Content(schema = @Schema(implementation = KamisException.PriceQueryPeriodTooLongException.class)))
    })
    @GetMapping("/getPrices")
    ResponseEntity<?> getPrices(
            @RequestParam("item_code")
            @Parameter(description = "상품 코드", example = "ITEM1")
            @NotBlank String itemCode,
            @RequestParam("category_code")
            @Parameter(description = "카테고리 코드", example = "CAT1")
            @NotBlank String categoryCode,
            @RequestParam("start_date")
            @Parameter(description = "조회 시작일 (yyyy-MM-dd)", example = "2025-03-01")
            @NotBlank String startDate,
            @RequestParam("end_date")
            @Parameter(description = "조회 종료일 (yyyy-MM-dd)", example = "2025-03-07")
            @NotBlank String endDate);

    @Operation(summary = "네이버 크롤링 실행", description = "네이버 트렌딩 키워드를 크롤링하여 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "크롤링 완료",
                    content = @Content(schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "Crawling completed successfully")))
    })
    @GetMapping("/naver/crawling")
    ResponseEntity<?> getCrawler();

    @Operation(summary = "네이버 트렌딩 키워드 조회", description = "네이버 트렌딩 키워드를 기반으로 최신 가격 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "트렌딩 키워드 가격 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = TrendingKeywordPrice.class)))
    })
    @GetMapping("/naver/trending")
    ResponseEntity<?> getTrendingKeywords();
}
