package it.compare.backend.scraping.rtvauroagd.scraper;

import it.compare.backend.product.model.Product;
import it.compare.backend.product.model.Shop;
import it.compare.backend.scraping.service.ScrapingService;
import it.compare.backend.scraping.shopcategorymap.ShopCategoryMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
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
        var products = new ArrayList<Product>();

        var futures = categories.entrySet().stream()
                .map(entry -> {
                    var category = entry.getKey();
                    var categoryValue = entry.getValue();

                    log.info("Starting scraping category: {}.", category);
                    return worker.scrapeCategory(category, categoryValue)
                            .thenAccept(products::addAll)
                            .thenRun(() -> log.info("Finished scraping category: {}.", category));
                })
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).thenRun(() -> {
            scrapingService.createProductsOrAddPriceStamp(products);
            log.info("Saved products to database.");
        });
    }
}
