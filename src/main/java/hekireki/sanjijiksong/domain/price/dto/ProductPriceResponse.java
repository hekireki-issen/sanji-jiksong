package hekireki.sanjijiksong.domain.price.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductPriceResponse {
    private PriceInfo info;
    private List<PriceHistory> history;
}
