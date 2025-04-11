package hekireki.sanjijiksong.domain.cart.dto;

public record CartItemRequestDto(
        Long cartId,
        Long itemId,
        int quantity
) {}