package hekireki.sanjijiksong.domain.cart.entity;

import hekireki.sanjijiksong.domain.user.entity.User;
import hekireki.sanjijiksong.global.common.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
public class Cart extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}












