package hekireki.sanjijiksong.domain.order.service;

import hekireki.sanjijiksong.domain.item.entity.Item;
import hekireki.sanjijiksong.domain.item.repository.ItemRepository;
import hekireki.sanjijiksong.domain.order.dto.OrderListUpdateRequest;
import hekireki.sanjijiksong.domain.order.dto.OrderRequest;
import hekireki.sanjijiksong.domain.order.dto.OrderResponse;
import hekireki.sanjijiksong.domain.order.entity.Order;
import hekireki.sanjijiksong.domain.order.entity.OrderList;
import hekireki.sanjijiksong.domain.order.entity.OrderStatus;
import hekireki.sanjijiksong.domain.order.repository.OrderRepository;
import hekireki.sanjijiksong.domain.user.entity.User;
import hekireki.sanjijiksong.global.common.exception.ItemException;
import hekireki.sanjijiksong.global.common.exception.OrderException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    // 주문 생성
    @Transactional
    public OrderResponse createOrder(User user, OrderRequest request) {
            Order order = new Order(user, OrderStatus.ORDERED);

            for (OrderRequest.OrderListRequest itemReq : request.orderLists()) {
                Item item = itemRepository.findById(itemReq.itemId())
                        .orElseThrow(ItemException.ItemNotFoundException::new);

                OrderList orderList = new OrderList(order, item, item.getStore(), itemReq.count());
                order.addOrderItem(orderList);
            }

            orderRepository.save(order);
            return OrderResponse.from(order);
    }

    // 주문 취소
    @Transactional
    public void cancelOrder(Long orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(OrderException.OrderNotFoundException::new);

        order.cancel(); // 도메인에서 상태 확인 및 예외 처리
    }

    // 주문 수정
    @Transactional
    public OrderResponse updateOrderItems(OrderListUpdateRequest request, User user) {
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(OrderException.OrderNotFoundException::new);

        if (!order.isUpdatable()) {
            throw new OrderException.OrderNotUpdatableException();
        }

        for (OrderListUpdateRequest.OrderListItemUpdate update : request.orderLists()) {
            OrderList target = order.getOrderLists().stream()
                    .filter(ol -> ol.getItem().getId().equals(update.itemId()))
                    .findFirst()
                    .orElseThrow(OrderException.OrderItemNotFoundException::new);

            int oldPrice = target.getCountPrice();
            int oldCount = target.getCount();

            target.updateCount(update.count());

            order.updateOrderSummary(oldCount, oldPrice, target.getCount(), target.getCountPrice());
        }

        return OrderResponse.from(order);
    }

    // 주문 상세 조회
    public OrderResponse getOrderDetail(Long orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(OrderException.OrderNotFoundException::new);

        return OrderResponse.from(order);
    }

    // 내 주문 목록 조회
    public List<OrderResponse> getMyOrders(User user) {
        return orderRepository.findAllByUser(user).stream()
                .map(OrderResponse::from)
                .toList();
    }

}
