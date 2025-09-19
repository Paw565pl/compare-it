package it.compare.backend.scraping.scheduler;

import it.compare.backend.scraping.service.ScrapingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScrapingScheduler {

    private final ScrapingService scrapingService;

    @Scheduled(cron = "0 0 */8 * * *")
    public void run() {
        scrapingService.scrapeAll();
    }
}
