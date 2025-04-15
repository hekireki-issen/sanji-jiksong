package hekireki.sanjijiksong.domain.openapi.entity;

import hekireki.sanjijiksong.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "`trending_keyword`",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "trending_keyword_unique",
                        columnNames = {"keyword", "category", "rank", "create_date"}
                )
        })
public class TrendingKeyword {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String keyword;
    private String category;
    @Column(name = "`rank`")
    private Integer rank;
    private LocalDate createDate;
}
