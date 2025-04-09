package hekireki.sanjijiksong.domain.openapi.service;

import hekireki.sanjijiksong.domain.openapi.Repository.TrendingKeywordRepository;
import hekireki.sanjijiksong.domain.price.entity.TrendingKeyword; // 엔티티 클래스 임포트 (필요에 따라 package 수정)
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrendingKeywordScheduler {

    private final TrendingKeywordRepository trendingKeywordRepository;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    public void collectTrendingKeywords() {
        // ChromeOptions 설정
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

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
