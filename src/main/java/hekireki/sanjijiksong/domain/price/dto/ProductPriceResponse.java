package hekireki.sanjijiksong.domain.price.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ProductPriceResponse(
        PriceInfo info, List<PriceHistory> history) {

}
