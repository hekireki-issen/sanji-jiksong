package hekireki.sanjijiksong.domain.item.entity;

import hekireki.sanjijiksong.domain.item.dto.ItemUpdateRequest;
import hekireki.sanjijiksong.domain.store.entity.Store;
import hekireki.sanjijiksong.global.common.BaseTimeEntity;
import hekireki.sanjijiksong.global.common.exception.ItemException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Entity
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
    private Integer price;

    private String image;

    @Column(nullable = false)
    private Integer stock;

    private String description;

    @Column(nullable = false)
    private Boolean active;

    @Enumerated(EnumType.STRING)
    private ItemStatus itemStatus;

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
//        if (dto.active() != null && !this.active.equals(dto.active())) {
//            this.active = dto.active();
//        }
        if (dto.itemStatus() != null && !this.itemStatus.equals(dto.itemStatus())) {
            this.itemStatus = dto.itemStatus();
        }
    }

    public void deactivate() {
        if (!this.active) {
            throw new ItemException.ItemAlreadyDeactivatedException();
        }
        this.active = false;
    }

    public void decreaseStock(int quantity) {
        if (this.stock < quantity) {
            throw new ItemException.ItemStockNotEnoughException();
        }

        this.stock -= quantity;

        if (this.stock == 0) {
            this.itemStatus = ItemStatus.SOLDOUT;
        }
    }

    public void addStock(int quantity) {
        this.stock += quantity;

        if (this.itemStatus == ItemStatus.SOLDOUT && this.stock > 0) {
            this.itemStatus = ItemStatus.ONSALE;
        }
    }

}