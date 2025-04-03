package hekireki.sanjijiksong.domain.order.entity;

import hekireki.sanjijiksong.domain.item.entity.Item;
import hekireki.sanjijiksong.domain.store.entity.Store;
import hekireki.sanjijiksong.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import org.aspectj.weaver.ast.Or;

public class OrderList extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;


    @Column(nullable = false)
    private int countPrice;
}