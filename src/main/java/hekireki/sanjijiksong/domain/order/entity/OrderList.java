package hekireki.sanjijiksong.domain.order.entity;

import hekireki.sanjijiksong.domain.item.entity.Item;
import hekireki.sanjijiksong.domain.store.entity.Store;
import hekireki.sanjijiksong.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.aspectj.weaver.ast.Or;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Entity
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
    private int count;

    @Column(nullable = false)
    private int countPrice;

    // 총 가격 계산
    public int calculateTotalPrice() {
        return item.getPrice() * count;
    }

    // 수량 변경 + 가격 재계산
    public void updateCount(int newCount) {
        this.count = newCount;
        this.countPrice = calculateTotalPrice();
    }

    public OrderList(Order order, Item item, Store store, int count) {
        this.order = order;
        this.item = item;
        this.store = store;
        this.count = count;
        this.countPrice = calculateTotalPrice();
    }
}