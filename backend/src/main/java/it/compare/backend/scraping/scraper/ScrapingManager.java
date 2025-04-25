package it.compare.backend.scraping.scraper;

import it.compare.backend.product.model.Shop;
import it.compare.backend.scraping.service.ScrapingService;
import it.compare.backend.scraping.shopcategorymap.ShopCategoryMap;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScrapingManager {

    private final ScrapingService scrapingService;
    private final ShopCategoryMap shopCategoryMap;
    private final List<ScraperWorker> scraperWorkers;

    private Map<Shop, ScraperWorker> scraperWorkerMap;

    @PostConstruct
    private void initializeWorkerMap() {
        scraperWorkerMap =
                scraperWorkers.stream().collect(Collectors.toMap(ScraperWorker::getShop, Function.identity()));
    }

    @SuppressWarnings("java:S6809")
    public void scrapeAll() {
        shopCategoryMap.getValues().keySet().forEach(shop -> CompletableFuture.runAsync(() -> this.scrapeShop(shop)));
    }

    @Async
    public void scrapeShop(Shop shop) {
        var worker = scraperWorkerMap.get(shop);
        if (worker == null) {
            log.error("worker not found for shop {}", shop);
            return;
        }

        var workerCategoryMap = shopCategoryMap.getValues().get(shop);
        if (workerCategoryMap == null) {
            log.error("category map not found for shop {}", shop);
            return;
        }

        workerCategoryMap.forEach((category, categoryLocator) -> {
            log.info("started scraping category {} for shop {}", category, shop);

            worker.scrapeCategory(category, categoryLocator)
                    .thenAccept(scrapingService::createProductsOrAddPriceStamp)
                    .thenRun(() -> log.info("finished scraping category {} for shop {}", category, shop))
                    .exceptionally(e -> {
                        log.error("error while scraping category {} for shop {} - {}", category, shop, e.getMessage());
                        return null;
                    });
        });
    }
}
