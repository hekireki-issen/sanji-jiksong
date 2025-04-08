package hekireki.sanjijiksong.domain.item.entity;

import hekireki.sanjijiksong.domain.item.dto.ItemUpdateRequest;
import hekireki.sanjijiksong.domain.store.entity.Store;
import hekireki.sanjijiksong.global.common.BaseTimeEntity;
import hekireki.sanjijiksong.global.common.exception.ErrorCode;
import hekireki.sanjijiksong.global.common.exception.ItemException;
import hekireki.sanjijiksong.global.common.exception.UserException;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus itemStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer price;

    private String image;

    @Column(nullable = false)
    private Integer stock;

    private String description;

    @Column(nullable = false)
    private Boolean active;

    // 피드백 후 수정예정
    private String category;

    public void updateIfChanged(ItemUpdateRequest dto) {
        if (dto.category() != null && !this.category.equals(dto.category())) {
            this.category = dto.category();
        }
        if (dto.itemName() != null && !this.name.equals(dto.itemName())) {
            this.name = dto.itemName();
        }
        if (dto.price() != null && !this.price.equals(dto.price())) {
            this.price = dto.price();
        }
        if (dto.image() != null && !this.image.equals(dto.image())) {
            this.image = dto.image();
        }
        if (dto.stock() != null && !this.stock.equals(dto.stock())) {
            this.stock = dto.stock();
        }
        if (dto.description() != null && !this.description.equals(dto.description())) {
            this.description = dto.description();
        }
        if (dto.active() != null && !this.active.equals(dto.active())) {
            this.active = dto.active();
        }
        if (dto.itemStatus() != null && !this.itemStatus.equals(dto.itemStatus())) {
            this.itemStatus = dto.itemStatus();
        }
    }

    public void deactivate() {
        if (!this.active) {
            throw new ItemException(ErrorCode.ITEM_ALREADY_DEACTIVATED);
        }
        this.active = false;
    }

}