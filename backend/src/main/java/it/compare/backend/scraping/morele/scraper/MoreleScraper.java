package it.compare.backend.scraping.morele.scraper;

import it.compare.backend.product.model.Shop;
import it.compare.backend.scraping.service.ScrapingService;
import it.compare.backend.scraping.shopcategorymap.ShopCategoryMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MoreleScraper {

    private static final Shop CURRENT_SHOP = Shop.MORELE_NET;

    private final ShopCategoryMap shopCategoryMap;
    private final ScrapingService scrapingService;
    private final MoreleScraperWorker moreleScraperWorker;

    public MoreleScraper(
            ShopCategoryMap shopCategoryMap, ScrapingService scrapingService, MoreleScraperWorker moreleScraperWorker) {
        this.shopCategoryMap = shopCategoryMap;
        this.scrapingService = scrapingService;
        this.moreleScraperWorker = moreleScraperWorker;
    }

    @Async
    public void scrape() {
        var categories = shopCategoryMap.getValues().get(CURRENT_SHOP);
        categories.forEach((category, categoryName) -> {
            log.info("Started scraping category: {}.", category);
            moreleScraperWorker
                    .scrapeCategory(category, categoryName)
                    .thenAccept(scrapingService::createProductsOrAddPriceStamp)
                    .thenRun(() -> log.info("Finished scraping category: {}.", category))
                    .exceptionally(e -> {
                        log.error("Error while scraping category: {}. {}", category, e.getMessage());
                        return null;
                    });
        });
    }
}
