package hekireki.sanjijiksong.domain.store.entity;

import hekireki.sanjijiksong.domain.user.entity.User;
import hekireki.sanjijiksong.global.common.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
public class Store extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String name;

    private String address;

    private String description;

    private String image;

    private Boolean active;
}
//