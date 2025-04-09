package hekireki.sanjijiksong.domain.openapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenApiScheduler {

    private final KamisPriceImportService kamisPriceImportService;
    private final TrendingKeywordService trendingKeywordService;

    //매일 13시 0분 0초에 실행
    @Scheduled(cron = "0 0 13 * * *")
    public void fetchKamisData() {
        LocalDate today = LocalDate.now();
        kamisPriceImportService.getAllPricesBetween(today, today);
    }



    // 매일 9시 0분 0초에 실행
    @Scheduled(cron = "0 0 9 * * *")
    public void fetchNaverTrendingKeywords() {
        trendingKeywordService.saveTodayTrendingKeywords();
    }
}
