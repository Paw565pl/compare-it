package it.compare.backend.scraping.rtveuroagd.watcher;

import it.compare.backend.scraping.rtveuroagd.scraper.RtvEuroAgdScraper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RtvEuroAgdWatcher {

    private final RtvEuroAgdScraper rtvEuroAgdScraper;

    public RtvEuroAgdWatcher(RtvEuroAgdScraper rtvEuroAgdScraper) {
        this.rtvEuroAgdScraper = rtvEuroAgdScraper;
    }

    @Scheduled(cron = "0 0 */6 * * *")
    public void startTask() {
        rtvEuroAgdScraper.scrape();
    }
}
