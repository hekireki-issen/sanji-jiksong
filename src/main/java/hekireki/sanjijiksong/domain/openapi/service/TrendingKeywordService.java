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

            // 상위 카테고리 "식품" 선택 (드롭다운 컨테이너 클릭 후 옵션 선택)
            WebElement categoryButton = driver.findElement(By.xpath("//*[@id='content']/div[2]/div/div[1]/div/div/div[1]/div/div[1]/span"));
            categoryButton.click();

            WebElement foodOption = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[@id='content']/div[2]/div/div[1]/div/div/div[1]/div/div[1]/ul/li[7]/a")
            ));
            foodOption.click();
            log.info("식품 카테고리 선택 완료.");
            Thread.sleep(2000);

            // 2분류 카테고리 옵션 인덱스와 이름 매핑 (li[1]=축산물, li[2]=수산물, li[3]=농산물)
            Map<Integer, String> subCategories = new HashMap<>();
            subCategories.put(1, "축산물");
            subCategories.put(2, "수산물");
            subCategories.put(3, "농산물");

            // 각 2분류 옵션별로 데이터 추출 및 DB 저장
            for (Map.Entry<Integer, String> entry : subCategories.entrySet()) {
                int liIndex = entry.getKey();
                String subCategoryName = entry.getValue();

                // 2분류 카테고리 버튼(컨테이너) 클릭하여 드롭다운 열기
                WebElement secondCategoryButton = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//*[@id='content']/div[2]/div/div[1]/div/div/div[1]/div/div[2]/span")
                ));
                secondCategoryButton.click();
                log.info("2분류 카테고리 버튼 클릭 완료.");
                Thread.sleep(2000); // 드롭다운 로딩 대기

                // 해당 옵션(li[index]) 선택
                String optionXPath = "//*[@id='content']/div[2]/div/div[1]/div/div/div[1]/div/div[2]/ul/li[" + liIndex + "]";
                WebElement subCategoryOption = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath(optionXPath)
                ));
                subCategoryOption.click();
                log.info("{} 카테고리 옵션 선택 완료.", subCategoryName);

                // 조회 버튼 클릭
                WebElement searchButton = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//*[@id='content']/div[2]/div/div[1]/div/a")
                ));
                searchButton.click();
                log.info("조회 버튼 클릭 완료 for {}.", subCategoryName);
                Thread.sleep(2000); // 조회 결과 로딩 대기

                // 인기 검색어 추출 (상위 10개)
                List<WebElement> keywordElements = driver.findElements(By.cssSelector("li a.link_text"));
                List<String> popularKeywords = new ArrayList<>();
                int count = 0;
                for (WebElement element : keywordElements) {
                    if (count >= 10) break;
                    String text = element.getText().trim();
                    // 예: "1한우" → 앞의 숫자와 공백 제거
                    String keyword = text.replaceAll("^\\d+\\s*", "");
                    popularKeywords.add(keyword);
                    count++;
                }
                log.info("[{}] 인기 검색어: {}", subCategoryName, popularKeywords);

                // 크롤링된 인기 검색어 데이터를 TrendingKeyword 엔티티로 변환하여 DB에 저장
                for (int i = 0; i < popularKeywords.size(); i++) {
                    String keyword = popularKeywords.get(i);
                    int rank = i + 1; // 순위는 1부터
                    TrendingKeyword trendingKeyword = TrendingKeyword.builder()
                            .category(subCategoryName)
                            .keyword(keyword)
                            .rank(rank)
                            .createDate(LocalDate.now())
                            .build();
                    trendingKeywordRepository.save(trendingKeyword);
                }
                log.info("[{}] 인기 검색어 저장 완료.", subCategoryName);

                // 다음 옵션 처리를 위한 대기
                Thread.sleep(2000);
            }

        } catch (Exception e) {
            log.error("크롤링 중 오류 발생", e);
        } finally {
            driver.quit();
        }
    }
}
