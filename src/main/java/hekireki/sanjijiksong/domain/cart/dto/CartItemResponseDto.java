package hekireki.sanjijiksong.domain.cart.dto;

public record CartItemResponseDto(
        Long itemId,
        String itemName,
        int price,
        String image,
        int quantity
) {
}