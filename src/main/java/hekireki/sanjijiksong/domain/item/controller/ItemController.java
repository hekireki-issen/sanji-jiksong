package hekireki.sanjijiksong.domain.item.controller;

import hekireki.sanjijiksong.domain.item.dto.ItemCreateRequest;
import hekireki.sanjijiksong.domain.item.dto.ItemResponse;
import hekireki.sanjijiksong.domain.item.dto.ItemUpdateRequest;
import hekireki.sanjijiksong.domain.item.service.ItemService;
import hekireki.sanjijiksong.global.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


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
    public ResponseEntity<ItemResponse> updateItem(@PathVariable Long storeId,
                                                   @PathVariable Long itemId,
                                                   @RequestBody ItemUpdateRequest itemUpdateRequest,
                                                   @AuthenticationPrincipal CustomUserDetails customUserDetails
    ){
        ItemResponse itemListResponse = itemService.updateItem(storeId, itemId, customUserDetails.getUsername(),itemUpdateRequest);
        return ResponseEntity.ok(itemListResponse);
    }

    @PatchMapping("{storeId}/items/{itemId}/deactivate")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> deactivateItem(@PathVariable Long storeId,
                                                       @PathVariable Long itemId,
                                                       @AuthenticationPrincipal CustomUserDetails customUserDetails
    ){
        itemService.deactivateItem(storeId, itemId, customUserDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> getSalesOverview(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        return itemService.getSalesOverview(customUserDetails.getUsername());
    }

    @GetMapping("/best-products")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> getTop5BestSellingProducts(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        return itemService.getTop5BestSellingProducts(customUserDetails.getUsername());
    }

    @GetMapping("/weekly-sales")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> getWeeklySalesTrend(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        return itemService.getWeeklySalesTrend(customUserDetails.getUsername());
    }

    @GetMapping("/hourly-sales")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> getDailyHourlySales(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                 @RequestParam LocalDateTime localDateTime
    ){
        return itemService.getDailyHourlySales(customUserDetails.getUsername(),localDateTime);
    }
}
