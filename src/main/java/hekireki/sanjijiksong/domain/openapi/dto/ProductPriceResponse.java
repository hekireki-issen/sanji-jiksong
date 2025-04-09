package hekireki.sanjijiksong.domain.openapi.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ProductPriceResponse(
        PriceInfo info, List<PriceHistory> history) {

}
