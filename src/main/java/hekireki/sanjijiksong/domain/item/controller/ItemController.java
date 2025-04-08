package hekireki.sanjijiksong.domain.item.controller;

import hekireki.sanjijiksong.domain.item.dto.ItemCreateRequest;
import hekireki.sanjijiksong.domain.item.dto.ItemResponse;
import hekireki.sanjijiksong.domain.item.dto.ItemUpdateRequest;
import hekireki.sanjijiksong.domain.item.service.ItemService;
import hekireki.sanjijiksong.global.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stores")
public class ItemController {

    private final ItemService itemService;

    @PostMapping("/{storeId}/items")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ItemResponse> createItem(@PathVariable Long storeId,
                                                   @RequestBody ItemCreateRequest itemCreateRequest,
                                                   @AuthenticationPrincipal CustomUserDetails customUserDetails
                                                   ){
        ItemResponse itemResponse = itemService.createItem(storeId, itemCreateRequest,customUserDetails.getUsername());
        return ResponseEntity.ok(itemResponse);
    }

    @GetMapping("/{storeId}/items")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<ItemResponse>> getMyItems (@PathVariable Long storeId,
                                                       @AuthenticationPrincipal CustomUserDetails customUserDetails
                                                       ){
        List<ItemResponse> itemListResponse = itemService.getMyItems(storeId,customUserDetails.getUsername());
        return ResponseEntity.ok(itemListResponse);
    }

    @GetMapping("{storeId}/items/{itemId}")
    public ResponseEntity<ItemResponse> getItemDetail(@PathVariable Long storeId,
                                                         @PathVariable Long itemId
                                                         ){
        ItemResponse itemListResponse = itemService.getItemDetail(storeId, itemId);
        return ResponseEntity.ok(itemListResponse);
    }

    @PatchMapping("{storeId}/items/{itemId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ItemResponse> updateProduct(@PathVariable Long storeId,
                                                      @PathVariable Long itemId,
                                                      @RequestBody ItemUpdateRequest itemUpdateRequest,
                                                      @AuthenticationPrincipal CustomUserDetails customUserDetails
    ){
        ItemResponse itemListResponse = itemService.updateItem(storeId, itemId, customUserDetails.getUsername(),itemUpdateRequest);
        return ResponseEntity.ok(itemListResponse);
    }
}
