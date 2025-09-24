package it.compare.backend.scraping.service;

import it.compare.backend.pricealert.service.PriceAlertService;
import it.compare.backend.product.model.ComputedState;
import it.compare.backend.product.model.Product;
import it.compare.backend.product.model.Shop;
import it.compare.backend.product.repository.ProductRepository;
import it.compare.backend.scraping.scraper.ScraperWorker;
import it.compare.backend.scraping.shopcategorymap.ShopCategoryMap;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrapingService {

    private final ProductRepository productRepository;
    private final PriceAlertService priceAlertService;
    private final ShopCategoryMap shopCategoryMap;
    private final List<ScraperWorker> scraperWorkers;

    private Map<Shop, ScraperWorker> scraperWorkerMap;

    @PostConstruct
    private void initializeWorkerMap() {
        scraperWorkerMap =
                scraperWorkers.stream().collect(Collectors.toMap(ScraperWorker::getShop, Function.identity()));
    }

    @Transactional
    public void createProductsOrAddPriceStamp(List<Product> scrapedProducts) {
        var eanList = scrapedProducts.stream().map(Product::getEan).toList();
        var existingProductsEanMap = productRepository.findAllByEanIn(eanList).stream()
                .collect(Collectors.toMap(Product::getEan, Function.identity()));

        var products = scrapedProducts.stream()
                .map(scrapedProduct -> {
                    var existingProduct = existingProductsEanMap.get(scrapedProduct.getEan());

                    // new product - immediately save it
                    if (existingProduct == null) {
                        var computedState = ComputedState.fromProduct(scrapedProduct);
                        scrapedProduct.setComputedState(computedState);

                        return scrapedProduct;
                    }

                    if (scrapedProduct.getOffers().isEmpty()) return null;
                    var newOffer = scrapedProduct.getOffers().getFirst();

                    if (newOffer.getPriceHistory().isEmpty()) return null;
                    var newPriceStamp = newOffer.getPriceHistory().getFirst();

                    var offers = existingProduct.getOffers();

                    // check if there is an existing offer from the scraped shop
                    var offerFromExistingShop = offers.stream()
                            .filter(o -> o.getShop().equals(newOffer.getShop()))
                            .findFirst();

                    offerFromExistingShop.ifPresentOrElse(
                            offer -> offer.addPriceStamp(newPriceStamp), () -> offers.add(newOffer));

                    var computedState = ComputedState.fromProduct(existingProduct);
                    existingProduct.setComputedState(computedState);

                    return existingProduct;
                })
                .filter(Objects::nonNull)
                .toList();

        if (!products.isEmpty()) {
            productRepository.saveAll(products);
            priceAlertService.checkPriceAlerts(products);
        }

        log.info("saved {} products", products.size());
    }

    @SuppressWarnings("java:S6809")
    public void scrapeAll() {
        shopCategoryMap.getValues().keySet().forEach(shop -> CompletableFuture.runAsync(() -> scrapeShop(shop)));
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
                    .thenAccept(this::createProductsOrAddPriceStamp)
                    .thenRun(() -> log.info("finished scraping category {} for shop {}", category, shop))
                    .exceptionally(e -> {
                        log.error(
                                "error of {} while scraping category {} for shop {} - {}",
                                e.getClass(),
                                category,
                                shop,
                                e.getMessage());
                        return null;
                    });
        });
    }
}
