package hekireki.sanjijiksong.domain.order.entity;

import hekireki.sanjijiksong.domain.user.entity.User;
import hekireki.sanjijiksong.global.common.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "`order`")
public class Order extends BaseTimeEntity {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(nullable = false)
	private OrderStatus orderStatus;

	@Column(nullable = false)
	private int stock;

}
