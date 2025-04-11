package hekireki.sanjijiksong.domain.cart.entity;

import hekireki.sanjijiksong.domain.item.entity.Item;
import hekireki.sanjijiksong.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "cart_item")

public class CartItem extends BaseTimeEntity {

    public CartItem() {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(nullable = false)
    private int quantity;

    // 생성자
    public CartItem(Cart cart, Item item, int quantity) {
        this.cart = cart;
        this.item = item;
        this.quantity = quantity;
    }

    // 수량 변경
    public void changeQuantity(int newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        this.quantity = newQuantity;
    }

    // 총 가격 계산
    public int getTotalPrice() {
        return item.getPrice() * quantity;
    }

    // 매진 여부 확인
    public boolean isSoldOut() {
        return item.getStock() == 0;
    }

    // CartItem.java 내부
    public void updateQuantity(int quantity) {
        this.quantity = quantity;
    }

    public static CartItem create(Cart cart, Item item, int quantity) {
        CartItem cartItem = new CartItem();
        cartItem.cart = cart;
        cartItem.item = item;
        cartItem.quantity = quantity;
        return cartItem;
    }

}
