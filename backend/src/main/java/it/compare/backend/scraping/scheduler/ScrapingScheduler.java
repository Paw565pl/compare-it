package it.compare.backend.scraping.scheduler;

import it.compare.backend.scraping.mediaexpert.scraper.MediaExpertScraper;
import it.compare.backend.scraping.morele.scraper.MoreleScraper;
import it.compare.backend.scraping.rtveuroagd.scraper.RtvEuroAgdScraper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScrapingScheduler {

    private final RtvEuroAgdScraper rtvEuroAgdScraper;
    private final MoreleScraper moreleScraper;
    private final MediaExpertScraper mediaExpertScraper;

    @Scheduled(cron = "0 0 */6 * * *")
    public void startScraping() {
        rtvEuroAgdScraper.scrape();
        moreleScraper.scrape();
        mediaExpertScraper.scrape();
    }
}
