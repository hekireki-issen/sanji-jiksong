package hekireki.sanjijiksong.domain.item.controller;

import hekireki.sanjijiksong.domain.item.api.ItemSearchApi;
import hekireki.sanjijiksong.domain.item.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ItemSearchController implements ItemSearchApi {

    private final ItemService itemService;

    @GetMapping("/items/search")
    public ResponseEntity<?> itemSearch(@RequestParam("keyword") String keyword,
                                        @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(itemService.itemSearch(keyword, pageable));
    }

    @GetMapping("/categories/search")
    public ResponseEntity<?> categorySearch(@RequestParam("keyword") String keyword,
                                            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(itemService.categorySearch(keyword, pageable));
    }
}
