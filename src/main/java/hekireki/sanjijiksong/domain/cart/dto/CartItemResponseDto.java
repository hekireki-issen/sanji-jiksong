package hekireki.sanjijiksong.domain.cart.dto;

import hekireki.sanjijiksong.domain.cart.entity.CartItem;

public class CartItemResponseDto {
    private final Long itemId;
    private final String itemName;
    private final int price;
    private final String image;
    private final int quantity;
    private final int totalPrice;
    private final String itemStatus;

    public CartItemResponseDto(Long itemId, String itemName, int price, String image, int quantity, int totalPrice, String itemStatus) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.price = price;
        this.image = image;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.itemStatus = itemStatus;
    }

    public Long itemId() { return itemId; }
    public String itemName() { return itemName; }
    public int price() { return price; }
    public String image() { return image; }
    public int quantity() { return quantity; }
    public int totalPrice() { return totalPrice; }
    public String itemStatus() { return itemStatus; }

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