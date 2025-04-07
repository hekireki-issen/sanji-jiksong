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
    private Integer currentPrice;
    private Integer oneDayAgoPrice;
    private Integer oneWeekAgoPrice;
    private Integer oneMonthAgoPrice;
    private String startDate;
    private String endDate;
}
