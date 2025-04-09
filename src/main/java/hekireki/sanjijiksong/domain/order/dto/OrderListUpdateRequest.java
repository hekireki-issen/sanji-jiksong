package hekireki.sanjijiksong.domain.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderListUpdateRequest(
        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
        int count
) {}
