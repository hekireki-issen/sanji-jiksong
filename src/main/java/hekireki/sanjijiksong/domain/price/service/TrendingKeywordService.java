package hekireki.sanjijiksong.domain.price.service;

import hekireki.sanjijiksong.domain.price.Repository.PriceDailyRepository;
import hekireki.sanjijiksong.domain.price.Repository.TrendingKeywordRepository;
import hekireki.sanjijiksong.domain.price.entity.PriceDaily;
import hekireki.sanjijiksong.domain.price.entity.TrendingKeyword;
import hekireki.sanjijiksong.domain.price.entity.TrendingKeywordPrice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrendingKeywordService {
    private final TrendingKeywordRepository trendingKeywordRepository;
    private final PriceDailyRepository priceDailyRepository;

    /**
     * 오늘 날짜의 TrendingKeyword 각각에 대해, PriceDaily 테이블에서 itemName에 해당 키워드가 포함된 최신 레코드를 조회하여
     * TrendingKeywordPrice로 반환합니다.
     *
     * 반환 타입은 Map<String, TrendingKeywordPriceDto>로, 키는 인기 검색어, 값은 해당 DTO입니다.
     */
    public Map<String, TrendingKeywordPrice> getLatestPriceInfoForTodayTrendingKeywords(){
        Map<String, TrendingKeywordPrice> priceInfoMap = new HashMap<>();

        // 오늘 날짜에 해당하는 TrendingKeyword만 조회합니다.
        List<TrendingKeyword> trendingKeywords = trendingKeywordRepository.findByCreateDate(LocalDate.now());

        for (TrendingKeyword tk : trendingKeywords) {
            String keyword = tk.getKeyword();

            // PriceDaily 테이블에서 itemName에 keyword가 포함된 최신 레코드 조회 (내림차순 정렬하여 첫 번째 건)
            PriceDaily priceDaily = priceDailyRepository.findTopByItemNameContainingOrderBySnapshotDateDesc(keyword);

            if (priceDaily != null) {
                // TrendingKeyword와 PriceDaily 정보를 결합하여 DTO 생성
                TrendingKeywordPrice dto = new TrendingKeywordPrice(
                        keyword,
                        tk.getCategory(),
                        tk.getRank(),
                        tk.getCreateDate(),
                        priceDaily.getItemName(),
                        priceDaily.getPrice(),
                        priceDaily.getSnapshotDate()
                );
                priceInfoMap.put(keyword, dto);
                log.info("키워드 [{}]에 대한 최신 가격 정보 DTO 생성: {}", keyword, dto);
            } else {
                log.info("키워드 [{}]에 매칭되는 가격 정보가 없습니다.", keyword);
            }
        }
        return priceInfoMap;
    }
}
