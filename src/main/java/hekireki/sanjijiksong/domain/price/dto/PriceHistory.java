package hekireki.sanjijiksong.domain.price.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PriceHistory {
    private String date;
    private Integer price;
}
