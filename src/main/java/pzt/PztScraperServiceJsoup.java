package pzt;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

@Service
public class PztScraperServiceJsoup {

    private static final String BASE_URL = "https://portal.pzt.pl/License.aspx?CategoryID=P";

    public List<String> szukajZawodnikowWDanymWieku(List<Integer> ageList, boolean zapiszDoCsv) {
        List<String> results = new ArrayList<>();
        List<String[]> csvRows = new ArrayList<>();

        int page = 0;
        boolean hasNext = true;

        while (hasNext) {
            try {
                Map<String, String> formData = new HashMap<>();
                formData.put("__EVENTTARGET", "ctl00$cphMainContainer$gvPlayers");
                formData.put("__EVENTARGUMENT", "Page$" + (page + 1));

                Document doc = fetchPage(page);
                if (doc == null) break;

                Element table = doc.selectFirst("table.list");
                if (table == null) break;

                Elements rows = table.select("tr");
                for (Element row : rows) {
                    Elements cells = row.select("td");
                    if (cells.size() >= 7) {
                        String login = cells.get(1).text().trim();
                        String nazwisko = cells.get(2).text().trim();
                        String imie = cells.get(3).text().trim();
                        String wiekText = cells.get(4).text().trim();
                        String klub = cells.get(5).text().trim();
                        String woj = cells.get(6).text().trim();

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

                // Sprawdź, czy jest "Następna"
                hasNext = doc.select("a:contains(Następna)").size() > 0;
                page++;

            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }

        if (zapiszDoCsv && !csvRows.isEmpty()) {
            zapiszCSV(csvRows);
        }

        return results;
    }

    private Document fetchPage(int page) {
        try {
            String url = BASE_URL + "&page=" + page;
            return Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();
        } catch (Exception e) {
            System.err.println("Błąd pobierania strony: " + e.getMessage());
            return null;
        }
    }

    private void zapiszCSV(List<String[]> rows) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("wyniki2.csv"))) {
            writer.println("Login,Nazwisko,Imię,Wiek,Klub,Województwo");
            for (String[] row : rows) {
                writer.println(String.join(",", row));
            }
            System.out.println("Zapisano plik: wyniki2.csv");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
