package it.compare.backend.scraping.mediaexpert.scraper;

import it.compare.backend.product.model.Shop;
import it.compare.backend.scraping.service.ScrapingService;
import it.compare.backend.scraping.shopcategorymap.ShopCategoryMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MediaExpertScraper {

    private static final Shop CURRENT_SHOP = Shop.MEDIA_EXPERT;

    private final ShopCategoryMap shopCategoryMap;
    private final ScrapingService scrapingService;
    private final MediaExpertScraperWorker worker;

    @Async
    public void scrape() {
        var categories = shopCategoryMap.getValues().get(CURRENT_SHOP);

        categories.forEach((category, categoryName) -> {
            log.info("started scraping category: {}", category);

            worker.scrapeCategory(category, categoryName)
                    .thenAccept(scrapingService::createProductsOrAddPriceStamp)
                    .thenRun(() -> log.info("finished scraping category - {}", category))
                    .exceptionally(e -> {
                        log.error("error while scraping category {} - {}", category, e.getMessage());
                        return null;
                    });
        });
    }
}
