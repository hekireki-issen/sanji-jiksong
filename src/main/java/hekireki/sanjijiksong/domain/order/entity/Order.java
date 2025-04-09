package hekireki.sanjijiksong.domain.order.entity;

import hekireki.sanjijiksong.domain.user.entity.User;
import hekireki.sanjijiksong.global.common.BaseTimeEntity;
import hekireki.sanjijiksong.global.common.exception.OrderException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Entity
@Table(name = "`ORDER`")
public class Order extends BaseTimeEntity {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderList> orderLists = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	private OrderStatus orderStatus;

	@Column(nullable = false)
	private int stock; // 총 수량

	@Column(nullable = false)
	private int totalPrice; // 총 금액

	// 주문 항목 추가
	public void addOrderItem(OrderList item) {
		this.orderLists.add(item);
		this.stock += item.getCount();
		this.totalPrice += item.getCountPrice();
	}

	// 총 주문 금액 재계산
	public int calculateTotalAmount() {
		return orderLists.stream()
				.mapToInt(OrderList::calculateTotalPrice)
				.sum();
	}

	// 총 주문 수량 재계산
	public int calculateTotalQuantity() {
		return orderLists.stream()
				.mapToInt(OrderList::getCount)
				.sum();
	}

	// 취소 가능 여부
	public boolean isCancelable() {
		return this.orderStatus == OrderStatus.ORDERED|| this.orderStatus == OrderStatus.PAID;
	}

	// 주문 취소 처리
	public void cancel() {
		if (!isCancelable()) {
			throw new OrderException.OrderNotCancelableException();
		}
		this.orderStatus = OrderStatus.CANCELED;
	}

	// 수정 가능 여부
	public boolean isUpdatable() {
		return this.orderStatus == OrderStatus.ORDERED;
	}

	// 주문 수정
	public void updateOrderSummary(int oldCount, int oldPrice, int newCount, int newPrice) {
		this.stock = this.stock - oldCount + newCount;
		this.totalPrice = this.totalPrice - oldPrice + newPrice;
	}

	public Order(User user, OrderStatus orderStatus) {
		this.user = user;
		this.orderStatus = orderStatus;
		this.stock = 0;
		this.totalPrice = 0;
	}
}
