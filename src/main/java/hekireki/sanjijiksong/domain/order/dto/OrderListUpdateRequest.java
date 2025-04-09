package hekireki.sanjijiksong.domain.order.dto;

import java.util.List;

public record OrderListUpdateRequest(
        Long orderId,
        List<OrderListItemUpdate> orderLists
) {
        public record OrderListItemUpdate(
                Long itemId,
                int count
        ) {}
}