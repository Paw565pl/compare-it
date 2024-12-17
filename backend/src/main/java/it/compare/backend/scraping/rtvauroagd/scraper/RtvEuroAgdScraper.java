package it.compare.backend.scraping.rtvauroagd.scraper;

import it.compare.backend.product.model.Shop;
import it.compare.backend.scraping.service.ScrapingService;
import it.compare.backend.scraping.shopcategorymap.ShopCategoryMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RtvEuroAgdScraper {

    private static final Shop CURRENT_SHOP = Shop.RTV_EURO_AGD;

    private final ShopCategoryMap shopCategoryMap;
    private final ScrapingService scrapingService;
    private final RtvEuroAgdScraperWorker worker;

    public RtvEuroAgdScraper(
            ShopCategoryMap shopCategoryMap, ScrapingService scrapingService, RtvEuroAgdScraperWorker worker) {
        this.shopCategoryMap = shopCategoryMap;
        this.scrapingService = scrapingService;
        this.worker = worker;
    }

    @Async
    public void scrape() {
        var categories = shopCategoryMap.getValues().get(CURRENT_SHOP);
        categories.forEach((category, categoryValue) -> {
            log.info("Started scraping category: {}.", category);

            worker.scrapeCategory(category, categoryValue)
                    .thenAccept(scrapingService::createProductsOrAddPriceStamp)
                    .thenRun(() -> log.info("Finished scraping category: {}.", category));
        });
    }
}
