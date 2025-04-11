package hekireki.sanjijiksong.domain.store.controller;
import hekireki.sanjijiksong.global.common.s3.S3Service;
import hekireki.sanjijiksong.global.security.dto.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
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


import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {
    private final StoreService storeService;
    private final S3Service s3Service;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<StoreResponse> createStore(
            @RequestPart("store") StoreCreateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {

        Long userId = userDetails.getUser().getId();
        log.info("가게 등록 요청 사용자 ID: {}, 요청 데이터: {}", userId, request);

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
        log.info("가게 등록 완료 결과: {}", response);

        return ResponseEntity.ok(response);
    }


    @GetMapping
    public ResponseEntity<?> getAllStores(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<StoreResponse> result = storeService.getAllActiveStores(pageable);
        log.info("가게 전체 조회 요청 - page={}, size={}", page, size);

        return ResponseEntity.ok(result);
    }



    @GetMapping("/{storeId}")
    public ResponseEntity<StoreResponse> getStoreById(@PathVariable("storeId") Long storeId) {
        StoreResponse response = storeService.getById(storeId);
        if(response == null)
            log.warn("특정 가게 조회 결과 존재하지 않는 storeId: {}", storeId);
        else
            log.info("특정 가게 조회 결과 storeId: {}, name: {}", storeId, response.name());
        return ResponseEntity.ok(response);
    }


    @GetMapping("/search")
    public ResponseEntity<List<StoreResponse>> searchStores(@RequestParam("keyword") String keyword) {
        List<StoreResponse> results = storeService.searchByKeyword(keyword);
        log.info("가게 검색 요청 keyword: {}", keyword);
        return ResponseEntity.ok(results);
    }


    @PatchMapping("/{storeId}/deactivate")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Void> deactivateStore(@PathVariable("storeId") Long storeId,
                                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        log.info("Patch: 가게 비활성화 - storeId={}, userId={}", storeId, userId);
        storeService.deactivate(storeId, userId);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping(value = "/{storeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<StoreResponse> updateStore(
            @PathVariable("storeId") Long storeId,
            @RequestPart("store") StoreUpdateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws IOException {
        Long userId = userDetails.getUser().getId();
        log.info("Patch : 가게 수정 - storeId={}, userId={}", storeId, userId);
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

