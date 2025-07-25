package pzt;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class TurniejRestController {

    @GetMapping(value = "/check", produces = "text/html; charset=UTF-8")
    public String sprawdzTurniej(
            @RequestParam String fraza,
            @RequestParam String data
    ) {
        String plikCsv = "wyniki.csv";
        List<String[]> znalezieni = new ArrayList<>();

        try {
            List<String> linie = Files.readAllLines(Paths.get(plikCsv));
            for (int i = 1; i < linie.size(); i++) {
                String[] pola = linie.get(i).split(",");
                if (pola.length < 3) continue;

                String login = pola[0].trim();
                String nazwisko = pola[1].trim();
                String imie = pola[2].trim();
                String wiek = pola[3].trim();
                String klub = pola[4].trim();
                String woj = pola.length > 5 ? pola[5].trim() : "---";

                String url = "https://portal.pzt.pl/PlayerTournament.aspx?UserID=" + login;

                try {
                    Document doc = Jsoup.connect(url)
                            .userAgent("Mozilla/5.0")
                            .timeout(10000)
                            .get();

                    Element tabela = doc.selectFirst("table.listBlue");
                    if (tabela == null) continue;

                    Elements rows = tabela.select("tr");
                    for (int r = 2; r < rows.size(); r++) {
                        Elements cols = rows.get(r).select("td");
                        if (cols.size() >= 5) {
                            String nazwaTurnieju = cols.get(1).text();
                            String dataTurnieju = cols.get(4).text();
                            if (nazwaTurnieju.toLowerCase().contains(fraza.toLowerCase())
                                    && dataTurnieju.contains(data)) {

                                znalezieni.add(new String[]{
                                        login, nazwisko, imie, wiek, woj, klub,
                                        nazwaTurnieju, dataTurnieju
                                });
                                break;
                            }
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Błąd pobierania dla: " + login);
                }
            }

        } catch (IOException e) {
            return "<p>Błąd odczytu pliku CSV</p>";
        }

        if (znalezieni.isEmpty()) {
            return "<p>Brak zawodników spełniających kryteria.</p>";
        }

        // Buduj HTML
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'><style>")
                .append("table { border-collapse: collapse; width: 100%; font-family: sans-serif; }")
                .append("th, td { border: 1px solid #ccc; padding: 8px; text-align: left; }")
                .append("th { background-color: #f2f2f2; }")
                .append("</style></head><body>");

        html.append("<h2>Zawodnicy biorący udział w turnieju zawierającym frazę „")
                .append(fraza)
                .append("” w dacie ")
                .append(data)
                .append("</h2>");

        html.append("<table><tr>")
                .append("<th>Login</th>")
                .append("<th>Nazwisko</th>")
                .append("<th>Imię</th>")
                .append("<th>Wiek</th>")
                .append("<th>Woj.</th>")
                .append("<th>Klub</th>")
                .append("<th>Turniej</th>")
                .append("<th>Data</th>")
                .append("<th>Profil</th>")
                .append("</tr>");

        for (String[] row : znalezieni) {
            html.append("<tr>");
            html.append("<td>").append(row[0]).append("</td>");
            html.append("<td>").append(row[1]).append("</td>");
            html.append("<td>").append(row[2]).append("</td>");
            html.append("<td>").append(row[3]).append("</td>");
            html.append("<td>").append(row[4]).append("</td>");
            html.append("<td>").append(row[5]).append("</td>");
            html.append("<td>").append(row[6]).append("</td>");
            html.append("<td>").append(row[7]).append("</td>");
            html.append("<td><a href='https://portal.pzt.pl/PlayerProfile.aspx?UserID=")
                    .append(row[0])
                    .append("' target='_blank'>Profil</a></td>");
            html.append("</tr>");
        }

        html.append("</table></body></html>");
        return html.toString();
    }
}
