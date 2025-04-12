package hekireki.sanjijiksong.domain.item.api;

import hekireki.sanjijiksong.domain.item.dto.ItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "ItemSearchApi", description = "상품 및 카테고리 검색 API")
public interface ItemSearchApi {

    @Operation(summary = "상품 검색", description = "상품 이름에 포함된 키워드로 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 검색 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ItemResponse.class),
                            examples = @ExampleObject(name = "상품 검색 결과 예시", value = """
                                {
                                  "content": [
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
                                  ],
                                  "totalPages": 1,
                                  "totalElements": 1,
                                  "last": true,
                                  "size": 10,
                                  "number": 0,
                                  "sort": { "empty": false, "sorted": true, "unsorted": false },
                                  "first": true,
                                  "numberOfElements": 1,
                                  "empty": false
                                }
                                """)))
    })
    @GetMapping("/api/v1/items/search")
    ResponseEntity<?> itemSearch(@RequestParam("keyword") String keyword,
                                 @PageableDefault(size = 10, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable);

    @Operation(summary = "카테고리 검색", description = "카테고리 이름에 포함된 키워드로 상품을 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "카테고리 검색 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ItemResponse.class),
                            examples = @ExampleObject(name = "카테고리 검색 결과 예시", value = """
                                {
                                  "content": [
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
                                  ],
                                  "totalPages": 1,
                                  "totalElements": 1,
                                  "last": true,
                                  "size": 10,
                                  "number": 0,
                                  "sort": { "empty": false, "sorted": true, "unsorted": false },
                                  "first": true,
                                  "numberOfElements": 1,
                                  "empty": false
                                }
                                """)))
    })
    @GetMapping("/api/v1/categories/search")
    ResponseEntity<?> categorySearch(@RequestParam("keyword") String keyword,
                                     @PageableDefault(size = 10, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable);
}