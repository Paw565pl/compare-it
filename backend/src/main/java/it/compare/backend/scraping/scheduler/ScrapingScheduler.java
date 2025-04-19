package it.compare.backend.scraping.scheduler;

import it.compare.backend.scraping.scraper.ScrapingManager;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScrapingScheduler {

    private final ScrapingManager scrapingManager;

    @Scheduled(fixedDelay = 2000000000) // cron = "0 0 */6 * * *"
    public void startScraping() {
        scrapingManager.scrapeAll();
    }
}
