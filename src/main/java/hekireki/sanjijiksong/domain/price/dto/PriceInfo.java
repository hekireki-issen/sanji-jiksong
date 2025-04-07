package hekireki.sanjijiksong.domain.price.dto;

import lombok.Builder;

@Builder
public record PriceInfo(
        String categoryCode,
        String itemCode,
        String itemName,
        String unit,
        Integer currentPrice,
        Integer oneDayAgoPrice,
        Integer oneWeekAgoPrice,
        Integer oneMonthAgoPrice,
        String startDate,
        String endDate
) { }
