package hekireki.sanjijiksong.domain.store.controller;
//import hekireki.sanjijiksong.domain.store.service.S3Service;
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

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {
    private final StoreService storeService;


    //가게 등록용
    @PostMapping
    public ResponseEntity<StoreResponse> createStore(@RequestBody StoreCreateRequest request,
                                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        System.out.println("로그인한 유저 ID = " + userId);  // 디버깅 로그

        StoreResponse response = storeService.create(request, userId);
        return ResponseEntity.ok(response);
    }

//    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<StoreResponse> createStore(
//            @RequestPart("store") StoreCreateRequest request,
//            @RequestPart("image") MultipartFile image,
//            @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {
//
//        Long userId = userDetails.getUser().getId();
//
//        String imageUrl = s3Service.uploadImage(image);
//        StoreCreateRequest updatedRequest = new StoreCreateRequest(
//                request.name(),
//                request.address(),
//                request.description(),
//                imageUrl
//        );
//
//        StoreResponse response = storeService.create(updatedRequest, userId);
//        return ResponseEntity.ok(response);
//    }


    //가게 조회용
    @GetMapping("/{storeId}")
    public ResponseEntity<StoreResponse> getStoreById(@PathVariable Long storeId) {
        StoreResponse response = storeService.getById(storeId);
        return ResponseEntity.ok(response);
    }


    @PatchMapping("/{storeId}/deactivate")
    public ResponseEntity<Void> deactivateStore(@PathVariable Long storeId,
                                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        storeService.deactivate(storeId, userId);
        return ResponseEntity.noContent().build();
    }

    //가게 수정용
    @PatchMapping("/{storeId}")
    public ResponseEntity<StoreResponse> updateStore(@PathVariable Long storeId,
                                                     @RequestBody StoreUpdateRequest request,
                                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        StoreResponse response = storeService.update(storeId, userId, request);
        return ResponseEntity.ok(response);
    }

}