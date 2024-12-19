package it.compare.backend.scraping.morele.watcher;

import it.compare.backend.scraping.morele.scraper.MoreleScraper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MoreleWatcher {

    private final MoreleScraper moreleScraper;

    public MoreleWatcher(MoreleScraper moreleScraper) {
        this.moreleScraper = moreleScraper;
    }

    @Scheduled(cron = "0 0 */2 * * *")
    public void startTask() {
        moreleScraper.scrape();
    }
}
