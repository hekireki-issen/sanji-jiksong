package hekireki.sanjijiksong.domain.openapi.dto;

import lombok.Builder;

@Builder
public record PriceInfo(
        String categoryCode,
        String itemCode,
        String itemName,
        String kindName,   // 추가된 필드
        String unit,
        Integer currentPrice,
        Integer oneDayAgoPrice,
        Integer oneWeekAgoPrice,
        Integer oneMonthAgoPrice,
        String startDate,
        String endDate
) { }
