package pzt;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PztScraperService {

    public List<String> szukajZawodnikowWDanymWieku(List<Integer> ageList, boolean zapiszDoCsv) {

        System.setProperty("webdriver.chrome.driver", "C:\\chromedriver\\chromedriver.exe"); // ← Zmień na własną ścieżkę

        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        List<String> results = new ArrayList<>();
        List<String[]> csvRows = new ArrayList<>();

        try {
            driver.get("https://portal.pzt.pl/License.aspx?CategoryID=P");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table.list")));

            boolean hasNext = true;
            while (hasNext) {
                WebElement table = driver.findElement(By.cssSelector("table.list"));
                List<WebElement> rows = table.findElements(By.tagName("tr"));

                for (WebElement row : rows) {
                    List<WebElement> cells = row.findElements(By.tagName("td"));
                    if (cells.size() >= 7) {
                        String login = cells.get(1).getText().trim();
                        String nazwisko = cells.get(2).getText().trim();
                        String imie = cells.get(3).getText().trim();
                        String wiekText = cells.get(4).getText().trim();
                        String klub = cells.get(5).getText().trim();
                        String woj = cells.get(6).getText().trim();

                        try {
                            int wiek = Integer.parseInt(wiekText);
                            if (ageList.contains(wiek)) {
                                String wynik = login + " | " + nazwisko + " " + imie + " | " + wiek + " lat | " + klub + " | " + woj;
                                results.add(wynik);
                                csvRows.add(new String[]{login, nazwisko, imie, String.valueOf(wiek), klub, woj});
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }

                List<WebElement> nextButtons = driver.findElements(By.linkText("Następna"));
                if (!nextButtons.isEmpty()) {
                    nextButtons.get(0).click();
                    Thread.sleep(1500);
                } else {
                    hasNext = false;
                }
            }

            if (zapiszDoCsv && !csvRows.isEmpty()) {
                zapiszCSV(csvRows);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }

        return results;
    }

    private void zapiszCSV(List<String[]> rows) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("wyniki.csv"))) {
            writer.println("Login,Nazwisko,Imię,Wiek,Klub,Województwo");
            for (String[] row : rows) {
                writer.println(String.join(",", row));
            }
            System.out.println("Zapisano plik: wyniki.csv");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
