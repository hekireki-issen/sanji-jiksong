package hekireki.sanjijiksong.domain.store.controller;
import hekireki.sanjijiksong.global.common.s3.S3Service;
import hekireki.sanjijiksong.global.security.dto.CustomUserDetails;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import hekireki.sanjijiksong.domain.store.dto.StoreCreateRequest;
import hekireki.sanjijiksong.domain.store.dto.StoreResponse;
import hekireki.sanjijiksong.domain.store.dto.StoreUpdateRequest;
import hekireki.sanjijiksong.domain.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {
    private final StoreService storeService;
    private final S3Service s3Service;

    //가게 등록용
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<StoreResponse> createStore(
            @RequestPart("store") StoreCreateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {

        Long userId = userDetails.getUser().getId();

        // 이미지를 안 넣는 가게도 있을 것 같음.
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = s3Service.uploadImage(image);
        }

        StoreCreateRequest updatedRequest = new StoreCreateRequest(
                request.name(),
                request.address(),
                request.description(),
                imageUrl != null ? imageUrl : ""
        );

        StoreResponse response = storeService.create(updatedRequest, userId);
        return ResponseEntity.ok(response);
    }

    //가게 전체 조회용
    @GetMapping
    public ResponseEntity<List<StoreResponse>> getAllStores() {
        List<StoreResponse> responses = storeService.getAllActiveStores();
        return ResponseEntity.ok(responses);
    }

    //가게 조회용
    @GetMapping("/{storeId}")
    public ResponseEntity<StoreResponse> getStoreById(@PathVariable("storeId") Long storeId) {
        StoreResponse response = storeService.getById(storeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<StoreResponse>> searchStores(
            @RequestParam("keyword") String keyword
    ) {
        List<StoreResponse> results = storeService.searchByKeyword(keyword);
        return ResponseEntity.ok(results);
    }


    //비활성화용
    @PatchMapping("/{storeId}/deactivate")
    public ResponseEntity<Void> deactivateStore(@PathVariable("storeId") Long storeId,
                                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        storeService.deactivate(storeId, userId);
        return ResponseEntity.noContent().build();
    }

   //가게 수정용
    @PatchMapping(value = "/{storeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<StoreResponse> updateStore(
            @PathVariable("storeId") Long storeId,
            @RequestPart("store") StoreUpdateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws IOException {
        Long userId = userDetails.getUser().getId();

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = s3Service.uploadImage(image);
        }

        StoreUpdateRequest updatedRequest = new StoreUpdateRequest(
                request.name(),
                request.address(),
                request.description(),
                imageUrl != null ? imageUrl : request.image()
        );

        StoreResponse response = storeService.update(storeId, userId, updatedRequest);
        return ResponseEntity.ok(response);
    }

}

