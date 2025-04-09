package hekireki.sanjijiksong.domain.item.controller;

import hekireki.sanjijiksong.domain.item.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ItemSearchController {

    private final ItemService itemService;

    @GetMapping("/items/search")
    public ResponseEntity<?> itemSearch(@RequestParam String keyword){
        return ResponseEntity.ok(itemService.itemSearch(keyword));
    }

    @GetMapping("/categories/search")
    public ResponseEntity<?> categorySearch(@RequestParam String keyword){
        return ResponseEntity.ok(itemService.categorySearch(keyword));
    }
}
