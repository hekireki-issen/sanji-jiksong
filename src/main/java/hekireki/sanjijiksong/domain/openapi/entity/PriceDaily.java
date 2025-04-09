package hekireki.sanjijiksong.domain.openapi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "`price_daily`",
uniqueConstraints = {
        @UniqueConstraint(
                name = "price_daily_unique",
                columnNames = {"category_code", "class_code", "item_name", "item_code", "kind_code", "rank_code", "snapshot_date"}
        )
})
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class PriceDaily {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 품목 관련 정보
    @Column(length = 3)
    private String categoryCode;
    @Column(length = 3)
    private String classCode;
    @Column(length = 50)
    private String itemName;
    @Column(length = 4)
    private String itemCode;
    @Column(length = 50)
    private String kindName;
    @Column(length = 2)
    private String kindCode;
    @Column(name = "`rank`")
    private String rank;
    private String rankCode;
    private String unit;

    // 가격 정보: snapshotType은 "당일"
    @Column(length = 10)
    private LocalDate snapshotDate; // 필요시 파싱 (예: "04/04" -> 실제 연도 포함 날짜)

    private Integer price;


    public void updatePrice(Integer dpr1) {
        this.price = dpr1;
    }
}
