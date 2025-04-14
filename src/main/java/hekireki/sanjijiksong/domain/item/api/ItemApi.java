package hekireki.sanjijiksong.domain.item.api;

import hekireki.sanjijiksong.domain.item.dto.ItemCreateRequest;
import hekireki.sanjijiksong.domain.item.dto.ItemResponse;
import hekireki.sanjijiksong.domain.item.dto.ItemUpdateRequest;
import hekireki.sanjijiksong.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "ItemController", description = "상품 관리 Controller")
@RequestMapping("/api/v1/stores")
public interface ItemApi{
        @Operation(summary = "상품 등록", description = "SELLER가 상품을 등록합니다.")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "등록 성공",
                        content = @Content(schema = @Schema(implementation = ItemResponse.class),
                                examples = @ExampleObject(name = "등록 성공 예시", value = """
                                {
                                  "id": 1,
                                  "storeId": 5,
                                  "name": "아메리카노",
                                  "price": 4500,
                                  "image": "https://cdn.example.com/image.png",
                                  "stock": 100,
                                  "description": "진한 커피입니다.",
                                  "active": true,
                                  "itemStatus": "ONSALE",
                                  "category": "커피"
                                }
                                """)))
        })
        @PostMapping(value = "/{storeId}/items", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasRole('SELLER')")
        ResponseEntity<ItemResponse> createItem(@PathVariable("storeId") Long storeId,
                                                @RequestPart("item") ItemCreateRequest request,
                                                @RequestPart("image") MultipartFile image,
                                                @AuthenticationPrincipal CustomUserDetails customUserDetails) throws IOException;

        @Operation(summary = "자신의 상품 조회", description = "SELLER가 등록한 상품 목록을 조회합니다.")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "상품 목록 조회 성공",
                        content = @Content(schema = @Schema(implementation = ItemResponse.class),
                                examples = @ExampleObject(name = "상품 목록 예시", value = """
                                [
                                  {
                                    "id": 1,
                                    "storeId": 5,
                                    "name": "아메리카노",
                                    "price": 4500,
                                    "image": "https://cdn.example.com/image1.png",
                                    "stock": 100,
                                    "description": "진한 커피입니다.",
                                    "active": true,
                                    "itemStatus": "ONSALE",
                                    "category": "커피"
                                  }
                                ]
                                """)))
        })
        @GetMapping("/{storeId}/items")
        @PreAuthorize("hasRole('SELLER')")
        ResponseEntity<List<ItemResponse>> getMyItems(@PathVariable Long storeId,
                                                      @AuthenticationPrincipal CustomUserDetails customUserDetails);

    @Operation(summary = "상품 수정", description = "SELLER가 상품 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 수정 성공",
                    content = @Content(schema = @Schema(implementation = ItemResponse.class),
                            examples = @ExampleObject(name = "수정 성공 예시", value = """
                                {
                                  "id": 1,
                                  "storeId": 5,
                                  "name": "아이스 아메리카노",
                                  "price": 4700,
                                  "image": "https://cdn.example.com/image_updated.png",
                                  "stock": 120,
                                  "description": "얼음이 추가된 진한 커피입니다.",
                                  "active": true,
                                  "itemStatus": "ONSALE",
                                  "category": "커피"
                                }
                                """)))
    })
    @PatchMapping(value = "/{storeId}/items/{itemId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SELLER')")
    ResponseEntity<ItemResponse> updateItem(@PathVariable("storeId") Long storeId,
                                            @PathVariable("itemId") Long itemId,
                                            @RequestPart("item") ItemUpdateRequest itemUpdateRequest,
                                            @RequestPart(value = "image", required = false) MultipartFile image,
                                            @AuthenticationPrincipal CustomUserDetails customUserDetails) throws IOException;

    @Operation(summary = "상품 비활성화", description = "SELLER가 상품을 비활성화합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "상품 비활성화 성공"),
            @ApiResponse(responseCode = "404", description = "상품이 존재하지 않음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "비활성화 실패 예시", value = """
                                {
                                  "status": 404,
                                  "message": "상품을 찾을 수 없습니다."
                                }
                                """)))
    })
    @PatchMapping("{storeId}/items/{itemId}/deactivate")
    @PreAuthorize("hasRole('SELLER')")
    ResponseEntity<?> deactivateItem(@PathVariable Long storeId,
                                     @PathVariable Long itemId,
                                     @AuthenticationPrincipal CustomUserDetails customUserDetails);

        @Operation(summary = "상품 상세 조회", description = "특정 상품의 상세 정보를 조회합니다.")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "상품 상세 조회 성공",
                        content = @Content(schema = @Schema(implementation = ItemResponse.class),
                                examples = @ExampleObject(name = "상품 상세 예시", value = """
                                {
                                  "id": 2,
                                  "storeId": 5,
                                  "name": "카페라떼",
                                  "price": 5000,
                                  "image": "https://cdn.example.com/image2.png",
                                  "stock": 80,
                                  "description": "우유가 들어간 커피입니다.",
                                  "active": true,
                                  "itemStatus": "ONSALE",
                                  "category": "커피"
                                }
                                """)))
        })
        @GetMapping("{storeId}/items/{itemId}")
        ResponseEntity<ItemResponse> getItemDetail(@PathVariable Long storeId,
                                                   @PathVariable Long itemId);
    @Operation(summary = "매출 현황 조회", description = "판매 중인 모든 상품의 누적 매출 데이터를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "매출 현황 조회 성공",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(name = "매출 현황 예시", value = """
                    {
                      "title": "품목 매출 현황",
                      "labels": ["아메리카노", "카페라떼"],
                      "series": [[120000, 85000]]
                    }
                """)))
    })
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('SELLER')")
    ResponseEntity<?> getSalesOverview(@AuthenticationPrincipal CustomUserDetails customUserDetails);

    @Operation(summary = "인기 상품 TOP 5", description = "판매금액 기준 상위 5개 상품 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "인기 상품 조회 성공",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(name = "TOP 5 예시", value = """
                    {
                      "xAxis": ["아메리카노", "카페라떼"],
                      "series": [
                        {
                          "name": "판매금액",
                          "data": [300000, 250000]
                        }
                      ]
                    }
                """)))
    })
    @GetMapping("/best-products")
    @PreAuthorize("hasRole('SELLER')")
    ResponseEntity<?> getTop5BestSellingProducts(@AuthenticationPrincipal CustomUserDetails customUserDetails);

    @Operation(summary = "주간 매출 추이 조회", description = "최근 7일간의 일자별 매출 현황 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "주간 매출 조회 성공",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(name = "주간 추이 예시", value = """
                    {
                      "title": "주간 매출 추이",
                      "xAxis": ["2024-04-01", "2024-04-02"],
                      "series": [
                        {
                          "name": "매출",
                          "data": [150000, 180000]
                        }
                      ]
                    }
                """)))
    })
    @GetMapping("/weekly-sales")
    @PreAuthorize("hasRole('SELLER')")
    ResponseEntity<?> getWeeklySalesTrend(@AuthenticationPrincipal CustomUserDetails customUserDetails);

    @Operation(summary = "시간대별 매출 조회", description = "선택한 날짜에 대한 시간대별 매출 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "시간대별 매출 조회 성공",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(name = "시간별 매출 예시", value = """
                    {
                      "title": "시간별 매출 추이",
                      "xAxis": ["09", "10", "11"],
                      "series": [
                        {
                          "name": "매출",
                          "data": [30000, 50000, 45000]
                        }
                      ]
                    }
                """)))
    })
    @GetMapping("/hourly-sales")
    @PreAuthorize("hasRole('SELLER')")
    ResponseEntity<?> getDailyHourlySales(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                          @RequestParam LocalDateTime localDateTime);

}
