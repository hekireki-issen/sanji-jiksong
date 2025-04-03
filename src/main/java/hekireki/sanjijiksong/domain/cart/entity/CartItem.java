package hekireki.sanjijiksong.domain.cart.entity;

import hekireki.sanjijiksong.domain.item.entity.Item;
import hekireki.sanjijiksong.global.common.BaseTimeEntity;
import jakarta.persistence.*;

public class CartItem extends BaseTimeEntity {

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
}
