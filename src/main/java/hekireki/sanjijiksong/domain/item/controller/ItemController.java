package hekireki.sanjijiksong.domain.item.controller;

import hekireki.sanjijiksong.domain.item.api.ItemApi;
import hekireki.sanjijiksong.domain.item.dto.ItemCreateRequest;
import hekireki.sanjijiksong.domain.item.dto.ItemResponse;
import hekireki.sanjijiksong.domain.item.dto.ItemUpdateRequest;
import hekireki.sanjijiksong.domain.item.service.ItemService;
import hekireki.sanjijiksong.global.common.s3.S3Service;
import hekireki.sanjijiksong.global.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.io.IOException;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stores")
public class ItemController implements ItemApi {

    private final ItemService itemService;
    private final S3Service s3Service;


    @PostMapping(value = "/items", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ItemResponse> createItem(
            @RequestPart("item") ItemCreateRequest request,
            @RequestPart("image") MultipartFile image,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) throws IOException {
        String imageUrl = s3Service.uploadImage(image);
        ItemCreateRequest updatedRequest = new ItemCreateRequest(
                request.category(),
                request.itemName(),
                request.price(),
                imageUrl,
                request.stock(),
                request.description()
        );
        ItemResponse response = itemService.createItem(updatedRequest, customUserDetails.getUsername());
        return ResponseEntity.ok(response);
    }


    @GetMapping("/items")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<ItemResponse>> getMyItems(@AuthenticationPrincipal CustomUserDetails customUserDetails
                                                       ){
        List<ItemResponse> itemListResponse = itemService.getMyItems(customUserDetails.getUsername());
        return ResponseEntity.ok(itemListResponse);
    }

    @GetMapping("{storeId}/items/{itemId}")
    public ResponseEntity<ItemResponse> getItemDetail(@PathVariable Long storeId,
                                                         @PathVariable Long itemId
                                                         ){
        ItemResponse itemListResponse = itemService.getItemDetail(storeId, itemId);
        return ResponseEntity.ok(itemListResponse);
    }


    @PatchMapping(value = "/items/{itemId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ItemResponse> updateItem(
            @PathVariable("itemId") Long itemId,
            @RequestPart("item") ItemUpdateRequest itemUpdateRequest,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) throws IOException {
        String imageUrl = null;

        if (image != null && !image.isEmpty()) {
            imageUrl = s3Service.uploadImage(image);
        }


        ItemUpdateRequest updatedRequest = new ItemUpdateRequest(
                itemUpdateRequest.category(),
                itemUpdateRequest.itemName(),
                itemUpdateRequest.price(),
                imageUrl != null ? imageUrl : itemUpdateRequest.image(),
                itemUpdateRequest.stock(),
                itemUpdateRequest.description(),
                itemUpdateRequest.itemStatus()
        );

        ItemResponse response = itemService.updateItem(itemId, customUserDetails.getUsername(), updatedRequest);
        return ResponseEntity.ok(response);
    }



    @PatchMapping("/items/{itemId}/deactivate")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> deactivateItem(@PathVariable Long itemId,
                                                       @AuthenticationPrincipal CustomUserDetails customUserDetails
    ){
        itemService.deactivateItem(itemId, customUserDetails.getUsername());
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
