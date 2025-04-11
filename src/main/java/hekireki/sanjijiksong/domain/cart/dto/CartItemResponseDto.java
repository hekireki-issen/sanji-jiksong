package hekireki.sanjijiksong.domain.cart.dto;

import hekireki.sanjijiksong.domain.cart.entity.CartItem;

public record CartItemResponseDto(
        Long itemId,
        String itemName,
        int price,
        String image,
        int quantity,
        int totalPrice,
        String itemStatus
) {
    public static CartItemResponseDto from(CartItem cartItem) {
        return new CartItemResponseDto(
                cartItem.getItem().getId(),
                cartItem.getItem().getName(),
                cartItem.getItem().getPrice(),
                cartItem.getItem().getImage(),
                cartItem.getQuantity(),
                cartItem.getTotalPrice(),
                cartItem.getItem().getItemStatus().toString()
        );
    }
}
