package hekireki.sanjijiksong.domain.store.controller;

import hekireki.sanjijiksong.domain.store.dto.StoreCreateRequest;
import hekireki.sanjijiksong.domain.store.dto.StoreResponse;
import hekireki.sanjijiksong.domain.store.dto.StoreUpdateRequest;
import hekireki.sanjijiksong.domain.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {
    private final StoreService storeService;

    //가게 등록용
    @PostMapping
    public ResponseEntity<StoreResponse> createStore(@RequestBody StoreCreateRequest request) {
        Long userId = 1L; // 현재는 임시 값
        StoreResponse response = storeService.create(request, userId);
        return ResponseEntity.ok(response);
    }

    //가게 조회용
    @GetMapping("/{storeId}")
    public ResponseEntity<StoreResponse> getStoreById(@PathVariable Long storeId) {
        StoreResponse response = storeService.getById(storeId);
        return ResponseEntity.ok(response);
    }

    //가게 수정용
    @PatchMapping("/{storeId}")
    public ResponseEntity<StoreResponse> updateStore(
            @PathVariable Long storeId,
            @RequestBody StoreUpdateRequest request
    ) {
        Long userId = 1L; // 임시유저
        StoreResponse response = storeService.update(storeId, userId, request);
        return ResponseEntity.ok(response);
    }

}