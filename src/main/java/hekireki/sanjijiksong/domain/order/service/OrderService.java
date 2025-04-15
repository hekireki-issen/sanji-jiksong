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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    // 주문 생성
    @Transactional
    public OrderResponse createOrder(User user, OrderRequest request) {
        log.info("주문 생성 요청 - userId={}, 요청 항목 수={}", user.getId(), request.orderLists().size());
        Order order = new Order(user, OrderStatus.ORDERED);

        for (OrderRequest.OrderListRequest itemReq : request.orderLists()) {
            Item item = itemRepository.findById(itemReq.itemId())
                    .orElseThrow(ItemException.ItemNotFoundException::new);

            if (item.getStock() < itemReq.count()) {
                log.warn("재고 부족 - itemId={}, 요청 수량={}, 현재 재고={}", item.getId(), itemReq.count(), item.getStock());
                throw new ItemException.ItemStockNotEnoughException();
            }

            item.decreaseStock(itemReq.count());
            log.info("재고 차감 - itemId={}, 차감 수량={}, 남은 재고={}", item.getId(), itemReq.count(), item.getStock());

            OrderList orderList = new OrderList(order, item, item.getStore(), itemReq.count());
            order.addOrderItem(orderList);
        }

        orderRepository.save(order);
        log.info("주문 생성 완료 - userId={}, orderId={}", user.getId(), order.getId());

        return OrderResponse.from(order);
    }

    // 주문 취소
    @Transactional
    public void cancelOrder(Long orderId, User user) {
        log.info("주문 취소 요청 - userId={}, orderId={}", user.getId(), orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(OrderException.OrderNotFoundException::new);

        order.cancel();

        for (OrderList orderList : order.getOrderLists()) {
            Item item = orderList.getItem();
            item.addStock(orderList.getCount());
            log.info("재고 복구 - itemId={}, 복구 수량={}, 현재 재고={}", item.getId(), orderList.getCount(), item.getStock());
        }

        log.info("주문 취소 완료 - userId={}, orderId={}", user.getId(), orderId);
    }

    // 주문 수정
    @Transactional
    public OrderResponse updateOrderItems(OrderListUpdateRequest request, User user) {
        log.info("주문 수정 요청 - userId={}, orderId={}", user.getId(), request.orderId());

        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(OrderException.OrderNotFoundException::new);

        if (!order.isUpdatable()) {
            log.warn("수정 불가 상태 - orderId={}, status={}", order.getId(), order.getOrderStatus());
            throw new OrderException.OrderNotUpdatableException();
        }

        for (OrderListUpdateRequest.OrderListItemUpdate update : request.orderLists()) {
            OrderList target = order.getOrderLists().stream()
                    .filter(ol -> ol.getItem().getId().equals(update.itemId()))
                    .findFirst()
                    .orElseThrow(OrderException.OrderItemNotFoundException::new);

            Item item = target.getItem();

            int oldPrice = target.getCountPrice();
            int oldCount = target.getCount();

            item.addStock(oldCount);
            item.decreaseStock(update.count());

            target.updateCount(update.count());

            order.updateOrderSummary(oldCount, oldPrice, target.getCount(), target.getCountPrice());

            log.info("주문 항목 수정 - orderId={}, itemId={}, 기존 수량={}, 변경 수량={}, 현재 재고={}",
                    order.getId(), item.getId(), oldCount, update.count(), item.getStock());
        }

        log.info("주문 수정 완료 - userId={}, orderId={}", user.getId(), order.getId());

        return OrderResponse.from(order);
    }

    // 주문 상세 조회
    public OrderResponse getOrderDetail(Long orderId, User user) {
        log.info("주문 상세 조회 요청 - userId={}, orderId={}", user.getId(), orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(OrderException.OrderNotFoundException::new);

        return OrderResponse.from(order);
    }

    // 내 주문 목록 조회
    public Page<OrderResponse> getMyOrders(User user, Pageable pageable) {
        log.info("내 주문 목록 조회 요청 - userId={}, page={}, size={}", user.getId(), pageable.getPageNumber(), pageable.getPageSize());

        return orderRepository.findAllByUser(user, pageable)
                .map(OrderResponse::from);
    }

}
