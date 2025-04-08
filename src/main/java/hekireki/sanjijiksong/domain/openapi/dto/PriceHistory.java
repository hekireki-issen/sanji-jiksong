package hekireki.sanjijiksong.domain.openapi.dto;


import lombok.Builder;

@Builder
public record PriceHistory(String date, Integer price) { }
