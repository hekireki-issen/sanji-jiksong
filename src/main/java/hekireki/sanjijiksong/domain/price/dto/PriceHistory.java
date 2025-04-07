package hekireki.sanjijiksong.domain.price.dto;


import lombok.Builder;

@Builder
public record PriceHistory(String date, Integer price) { }
