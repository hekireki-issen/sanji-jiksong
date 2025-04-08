package hekireki.sanjijiksong.domain.item.entity;

import hekireki.sanjijiksong.domain.store.entity.Store;
import hekireki.sanjijiksong.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int price;

    private String image;

    @Column(nullable = false)
    private int stock;

    private String description;

    @Column(nullable = false)
    private Boolean active;

    @Column(nullable = false)
    private ItemStatus itemStatus;

    // 피드백 후 수정예정
    private String category;
}