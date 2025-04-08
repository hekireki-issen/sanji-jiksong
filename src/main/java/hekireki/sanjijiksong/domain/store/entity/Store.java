package hekireki.sanjijiksong.domain.store.entity;

import hekireki.sanjijiksong.domain.user.entity.User;
import hekireki.sanjijiksong.global.common.BaseTimeEntity;
import hekireki.sanjijiksong.global.common.exception.ErrorCode;
import hekireki.sanjijiksong.global.common.exception.StoreException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Store extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    private String address;

    private String description;

    private String image;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    public void deactivate() {
        if (!this.active) {
            throw new StoreException(ErrorCode.STORE_ALREADY_DEACTIVATED);
        }
        this.active = false;
    }
    public void update(String name, String address, String description, String image) {
        if (name != null)
            this.name = name;
        if (address != null)
            this.address = address;
        if (description != null)
            this.description = description;
        if (image != null)
            this.image = image;
    }


}
//


