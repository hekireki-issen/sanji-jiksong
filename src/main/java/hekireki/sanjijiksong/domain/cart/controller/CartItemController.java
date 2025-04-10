package hekireki.sanjijiksong.domain.cart.controller;

import hekireki.sanjijiksong.domain.cart.dto.CartItemRequestDto;
import hekireki.sanjijiksong.domain.cart.dto.CartItemResponseDto;
import hekireki.sanjijiksong.domain.cart.service.CartItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart-items")
public class CartItemController {

    private final CartItemService cartItemService;

    // 장바구니에 상품 추가
    @PostMapping
    public ResponseEntity<CartItemResponseDto> addCartItem(@RequestBody CartItemRequestDto requestDto) {
        CartItemResponseDto response = cartItemService.addCartItem(
                requestDto.cartId(), requestDto.itemId(), requestDto.quantity());
        return ResponseEntity.ok(response);
    }

    // 특정 장바구니 내 상품 목록 조회
    @GetMapping("/{cartId}")
    public ResponseEntity<List<CartItemResponseDto>> getCartItems(@PathVariable Long cartId) {
        List<CartItemResponseDto> cartItems = cartItemService.getCartItems(cartId);
        return ResponseEntity.ok(cartItems);
    }

    // 장바구니 상품 수량 수정
    @PutMapping("/{cartItemId}")
    public ResponseEntity<Void> updateCartItem(@PathVariable Long cartItemId,
                                               @RequestParam int quantity) {
        cartItemService.updateCartItemQuantity(cartItemId, quantity);
        return ResponseEntity.ok().build();
    }

    // 장바구니 상품 삭제
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> deleteCartItem(@PathVariable Long cartItemId) {
        cartItemService.removeCartItem(cartItemId);
        return ResponseEntity.noContent().build();
    }
}