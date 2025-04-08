package hekireki.sanjijiksong.domain.cart.dto;

import java.util.List;

public record CartResponseDto(
        Long cartId,
        Long userId,
        List<CartItemResponseDto> items,
        int totalPrice
) {
}