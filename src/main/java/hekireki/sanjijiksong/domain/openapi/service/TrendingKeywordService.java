package hekireki.sanjijiksong.domain.openapi.service;

import hekireki.sanjijiksong.domain.openapi.Repository.PriceDailyRepository;
import hekireki.sanjijiksong.domain.openapi.Repository.TrendingKeywordRepository;
import hekireki.sanjijiksong.domain.openapi.dto.TrendingKeywordPrice;
import hekireki.sanjijiksong.domain.openapi.entity.PriceDaily;
import hekireki.sanjijiksong.domain.price.entity.TrendingKeyword;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
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
     * <p>
     * 반환 타입은 Map<String, TrendingKeywordPriceDto>로, 키는 인기 검색어, 값은 해당 DTO입니다.
     */
    public Map<String, TrendingKeywordPrice> getTrendingKeywordsPriceInfo() {
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

    public void saveTodayTrendingKeywords() {
        // ChromeOptions 설정
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1920,1080", "--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage");

        // WebDriver 객체 생성
        WebDriver driver = new ChromeDriver(options);

        try {
            log.info("크롤링 시작");
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
            driver.get("https://datalab.naver.com/shoppingInsight/sCategory.naver");

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
            wait.until(dr -> ((org.openqa.selenium.JavascriptExecutor) dr)
                    .executeScript("return document.readyState").equals("complete"));

            // 상위 카테고리 "식품" 선택
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id='content']/div[2]/div/div[1]/div/div/div[1]/div/div[1]/span")));
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id='content']/div[2]/div/div[1]/div/div/div[1]/div/div[1]/ul/li[7]/a"))).click();
            log.info("식품 카테고리 선택 완료.");


            // 2분류 카테고리 옵션 인덱스와 이름 매핑 (li[1]=축산물, li[2]=수산물, li[3]=농산물)
            Map<Integer, String> subCategories = Map.of(1, "축산물", 2, "수산물", 3, "농산물");

            // 각 2분류 옵션별로 데이터 추출 및 DB 저장
            for (Map.Entry<Integer, String> entry : subCategories.entrySet()) {
                int liIndex = entry.getKey();
                String subCategoryName = entry.getValue();

                // 2분류 카테고리 버튼(컨테이너) 클릭하여 드롭다운 열기
                wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//*[@id='content']/div[2]/div/div[1]/div/div/div[1]/div/div[2]/span")));
                Thread.sleep(2000); // 드롭다운 로딩 대기

                // 해당 옵션(li[index]) 선택
                wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id='content']/div[2]/div/div[1]/div/div/div[1]/div/div[2]/ul/li[" + liIndex + "]"))).click();
                log.info("{} 카테고리 옵션 선택 완료.", subCategoryName);

                // 조회 버튼 클릭
                wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id='content']/div[2]/div/div[1]/div/a"))).click();
                log.info("조회 버튼 클릭 완료 for {}.", subCategoryName);
                Thread.sleep(2000);

                // 인기 검색어 추출 (상위 10개)
                List<WebElement> keywordElements = driver.findElements(By.cssSelector("li a.link_text"));
                List<String> popularKeywords = new ArrayList<>();
                for (int i = 0; i < Math.min(10, keywordElements.size()); i++) {
                    popularKeywords.add(keywordElements.get(i).getText().trim().replaceAll("^\\d+\\s*", ""));
                }
                log.info("[{}] 인기 검색어: {}", subCategoryName, popularKeywords);

                for (int i = 0; i < popularKeywords.size(); i++) {
                    trendingKeywordRepository.save(TrendingKeyword.builder()
                            .category(subCategoryName)
                            .keyword(popularKeywords.get(i))
                            .rank(i + 1)
                            .createDate(LocalDate.now())
                            .build());
                }
                log.info("[{}] 인기 검색어 저장 완료.", subCategoryName);
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            log.error("크롤링 중 오류 발생", e);
        } finally {
            driver.quit();
        }
    }
}
