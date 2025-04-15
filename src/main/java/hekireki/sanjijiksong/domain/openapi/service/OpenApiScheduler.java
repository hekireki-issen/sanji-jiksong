package hekireki.sanjijiksong.domain.openapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;


/**
 * OpenAPI 관련 스케줄러
 * 매일 정해진 시간에 KAMIS API에서 가격 정보를 가져오고,
 * 네이버 트렌딩 키워드를 가져오는 스케줄러
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OpenApiScheduler {

    private final KamisPriceImportService kamisPriceImportService;
    private final TrendingKeywordService trendingKeywordService;


    //매일 13시 0분 0초에 실행
    @Async
    @Scheduled(cron = "0 0 13 * * *")
    public void fetchKamisData() {
        log.info("fetchKamisData 스케쥴러 실행");
        LocalDate today = LocalDate.now();
        kamisPriceImportService.getAllPricesBetween(today, today);
        log.info("fetchKamisData 스케쥴러 종료");
    }



    // 매일 9시 0분 0초에 실행
    @Async
    @Scheduled(cron = "0 0 9 * * *")
    public void fetchNaverTrendingKeywords() {
        log.info("fetchNaverTrendingKeywords 스케쥴러 실행");
        trendingKeywordService.saveTodayTrendingKeywords();
        log.info("fetchNaverTrendingKeywords 스케쥴러 종료");
    }
}
