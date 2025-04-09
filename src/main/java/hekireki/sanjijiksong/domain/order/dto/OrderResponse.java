package hekireki.sanjijiksong.domain.order.dto;

import hekireki.sanjijiksong.domain.order.entity.Order;
import hekireki.sanjijiksong.domain.order.entity.OrderList;
import hekireki.sanjijiksong.domain.order.entity.OrderStatus;
import lombok.Builder;

import java.util.List;

public record OrderResponse(
        Long orderId,
        OrderStatus orderStatus,
        int totalCount,
        int totalPrice,
        List<OrderListResponse> orderLists
) {
    @Builder
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderStatus(),
                order.getStock(),
                order.getTotalPrice(),
                order.getOrderLists().stream()
                        .map(OrderListResponse::from)
                        .toList()
        );
    }

    public record OrderListResponse(
            Long itemId,
            String itemName,
            int price,
            int count,
            int totalPrice,
            Long storeId,
            String storeName
    ) {
        public static OrderListResponse from(OrderList ol) {
            return new OrderListResponse(
                    ol.getItem().getId(),
                    ol.getItem().getName(),
                    ol.getItem().getPrice(),
                    ol.getCount(),
                    ol.getCountPrice(),
                    ol.getStore().getId(),
                    ol.getStore().getName()
            );
        }
    }
}
