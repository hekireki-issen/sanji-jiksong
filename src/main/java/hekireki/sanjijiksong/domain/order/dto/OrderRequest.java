package hekireki.sanjijiksong.domain.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderRequest(
        @NotEmpty(message = "주문 항목은 최소 1개 이상이어야 합니다.")
        List<@Valid OrderListRequest> orderLists
) {
    public record OrderListRequest(
            @NotNull(message = "상품 ID는 필수입니다.")
            Long itemId,

            @Min(value = 1, message = "최소 1개 이상 주문해야 합니다.")
            int count
    ) {}
}
