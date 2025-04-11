package hekireki.sanjijiksong.domain.store.api;

import hekireki.sanjijiksong.domain.store.dto.StoreCreateRequest;
import hekireki.sanjijiksong.domain.store.dto.StoreResponse;
import hekireki.sanjijiksong.domain.store.dto.StoreUpdateRequest;
import hekireki.sanjijiksong.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.io.IOException;
import java.util.List;

@Tag(name = "StoreController", description = "가게 관련 API")
@RequestMapping("/api/v1/stores")
public interface StoreApi {

    @Operation(summary = "가게 등록", description = "판매자가 가게를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "등록 성공",
                    content = @Content(schema = @Schema(implementation = StoreResponse.class),
                            examples = @ExampleObject(value = """
                                {
                                  "id": 1,
                                  "name": "카페 산지",
                                  "address": "서울시 종로구",
                                  "description": "커피가 맛있는 집",
                                  "image": "https://image.url",
                                  "active": true
                                }
                            """)))
    })
    @PostMapping(consumes = "multipart/form-data")
    ResponseEntity<StoreResponse> createStore(
            @RequestPart("store") StoreCreateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal CustomUserDetails userDetails
    )throws IOException;

    @Operation(summary = "가게 전체 조회", description = "활성화된 가게 목록을 페이징으로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    ResponseEntity<?> getAllStores(@RequestParam(value = "page", defaultValue = "0") int page,
                                   @RequestParam(value = "size", defaultValue = "10") int size);

    @Operation(summary = "가게 상세 조회", description = "storeId로 특정 가게를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{storeId}")
    ResponseEntity<StoreResponse> getStoreById(@PathVariable("storeId") Long storeId);

    @Operation(summary = "가게 키워드 검색", description = "키워드로 가게를 검색합니다.")
    @ApiResponse(responseCode = "200", description = "검색 성공")
    @GetMapping("/search")
    ResponseEntity<List<StoreResponse>> searchStores(@RequestParam("keyword") String keyword);

    @Operation(summary = "가게 비활성화", description = "판매자가 가게를 비활성화합니다.")
    @ApiResponse(responseCode = "204", description = "비활성화 성공")
    @PatchMapping("/{storeId}/deactivate")
    ResponseEntity<Void> deactivateStore(@PathVariable("storeId") Long storeId,
                                         @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "가게 수정", description = "판매자가 가게 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = StoreResponse.class),
                            examples = @ExampleObject(value = """
                                {
                                  "id": 1,
                                  "name": "수정상점",
                                  "address": "부산",
                                  "description": "수정된 설명",
                                  "image": "https://image.updated.url",
                                  "active": true
                                }
                            """)))
    })
    @PatchMapping(value = "/{storeId}", consumes = "multipart/form-data")
    ResponseEntity<StoreResponse> updateStore(
            @PathVariable("storeId") Long storeId,
            @RequestPart("store") StoreUpdateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws IOException;;
}
