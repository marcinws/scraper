package pzt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PztScraperController {

    @Autowired
    private PztScraperService seleniumService;

    @Autowired
    private PztScraperServiceJsoup jsoupService;

    @GetMapping("/szukaj")
    public List<String> szukajZawodnikow(
            @RequestParam List<Integer> age,
            @RequestParam(required = false, defaultValue = "false") boolean csv,
            @RequestParam(required = false, defaultValue = "jsoup") String method
    ) {
        if (age.size() > 3) {
            throw new IllegalArgumentException("Maksymalnie 3 wartości wieku.");
        }

        if ("selenium".equalsIgnoreCase(method)) {
            return seleniumService.szukajZawodnikowWDanymWieku(age, csv);
        } else if ("jsoup".equalsIgnoreCase(method)) {
            return jsoupService.szukajZawodnikowWDanymWieku(age, csv);
        } else {
            throw new IllegalArgumentException("Nieznana metoda: " + method + ". Dozwolone: jsoup, selenium.");
        }
    }
}
