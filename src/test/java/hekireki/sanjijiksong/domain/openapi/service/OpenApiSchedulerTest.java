package hekireki.sanjijiksong.domain.openapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class OpenApiSchedulerTest {

    @Mock
    private KamisPriceImportService kamisPriceImportService;

    @Mock
    private TrendingKeywordService trendingKeywordService;

    @InjectMocks
    private OpenApiScheduler scheduler;

    @Test
    public void testFetchKamisData() {
        // arrange: 현재 날짜를 기준으로 메서드 내부의 LocalDate.now()와 동일한 값을 기대합니다.
        LocalDate today = LocalDate.now();

        // act: fetchKamisData() 메서드를 직접 호출.
        scheduler.fetchKamisData();

        // assert: KamisPriceImportService의 getAllPricesBetween 메서드가 (오늘, 오늘) 인자로 호출되었는지 검증.
        verify(kamisPriceImportService).getAllPricesBetween(eq(today), eq(today));
    }

    @Test
    public void testFetchNaverTrendingKeywords() {
        // act: fetchNaverTrendingKeywords() 메서드 호출.
        scheduler.fetchNaverTrendingKeywords();

        // assert: TrendingKeywordService의 saveTodayTrendingKeywords 메서드가 호출되었는지 검증.
        verify(trendingKeywordService).saveTodayTrendingKeywords();
    }
}
