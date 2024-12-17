package it.compare.backend.scraping.rtvauroagd.watcher;

import it.compare.backend.scraping.rtvauroagd.scraper.RtvEuroAgdScraper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RtvEuroAgdWatcher {

    private final RtvEuroAgdScraper rtvEuroAgdScraper;

    public RtvEuroAgdWatcher(RtvEuroAgdScraper rtvEuroAgdScraper) {
        this.rtvEuroAgdScraper = rtvEuroAgdScraper;
    }

    @Scheduled(cron = "0 0 */2 * * *")
    public void startTask() {
        rtvEuroAgdScraper.scrape();
    }
}
