package hekireki.sanjijiksong.domain.price.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class PriceInfo {
    private String categoryCode;
    private String itemCode;
    private String itemName;
    private String unit;
    private String currentPrice;
    private String oneDayAgoPrice;
    private String oneWeekAgoPrice;
    private String oneMonthAgoPrice;
    private String startDate;
    private String endDate;
}
