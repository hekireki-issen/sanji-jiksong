package hekireki.sanjijiksong.domain.price.entity;

import java.time.LocalDate;

public record TrendingKeywordPrice(
        String keyword,               // 인기 검색어
        String category,              // TrendingKeyword에 저장된 상위 카테고리 ("식품")
        Integer trendingRank,         // 인기 검색어 순위
        LocalDate trendingCreateDate, // TrendingKeyword 생성일 (크롤링 일자)
        String itemName,              // PriceDaily의 품목명
        Integer price,                // 가격
        LocalDate snapshotDate
) {
}
