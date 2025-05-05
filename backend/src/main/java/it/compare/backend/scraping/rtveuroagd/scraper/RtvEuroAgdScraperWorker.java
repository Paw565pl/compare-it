package it.compare.backend.scraping.rtveuroagd.scraper;

import generator.RandomUserAgentGenerator;
import it.compare.backend.product.model.*;
import it.compare.backend.scraping.rtveuroagd.dto.RtvEuroAgdProduct;
import it.compare.backend.scraping.rtveuroagd.dto.RtvEuroAgdResponse;
import it.compare.backend.scraping.scraper.ScraperWorker;
import it.compare.backend.scraping.util.ScrapingUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
class RtvEuroAgdScraperWorker implements ScraperWorker {

    private static final Shop CURRENT_SHOP = Shop.RTV_EURO_AGD;
    private static final String BASE_URL = "https://www.euro.com.pl/rest/api/products/search";

    private final RestClient restClient;
    private final Semaphore rtvEuroAgdSemaphore;

    private static final int PAGE_SIZE = 25;

    @Async
    @Override
    public CompletableFuture<List<Product>> scrapeCategory(Category category, String categoryLocator) {
        var isSemaphorePermitAcquired = false;

        try {
            rtvEuroAgdSemaphore.acquire();
            isSemaphorePermitAcquired = true;

            var currentStartFrom = 0;
            var products = new ArrayList<Product>();

            while (true) {
                var productPage = fetchProductPage(currentStartFrom, category, categoryLocator);
                if (productPage == null
                        || productPage.results() == null
                        || productPage.results().isEmpty()) break;

                var newProducts = productPage.results().stream()
                        .map(productResponse -> createProduct(category, productResponse))
                        .filter(Objects::nonNull)
                        .toList();
                products.addAll(newProducts);

                currentStartFrom += PAGE_SIZE;
                ScrapingUtil.sleep();
            }

            return CompletableFuture.completedFuture(products);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("thread was interrupted while acquiring semaphore for scraping category {}", category);

            return CompletableFuture.failedFuture(e);
        } finally {
            if (isSemaphorePermitAcquired) rtvEuroAgdSemaphore.release();
        }
    }

    @Override
    public Shop getShop() {
        return CURRENT_SHOP;
    }

    private RtvEuroAgdResponse fetchProductPage(int currentStartFrom, Category category, String categoryLocator) {
        var uri = UriComponentsBuilder.fromUriString(BASE_URL)
                .queryParam("numberOfItems", PAGE_SIZE)
                .queryParam("startFrom", currentStartFrom)
                .queryParam("category", categoryLocator)
                .queryParam("developSearchMode", "false")
                .build()
                .toUri();

        try {
            return restClient
                    .get()
                    .uri(uri)
                    .header("Accept", "application/json")
                    .header("User-Agent", RandomUserAgentGenerator.getNext())
                    .retrieve()
                    .body(RtvEuroAgdResponse.class);
        } catch (HttpStatusCodeException e) {
            log.warn(
                    "http error has occurred while scraping category {} on page start index {} - {}",
                    category,
                    currentStartFrom,
                    e.getStatusCode().value());
        } catch (ResourceAccessException e) {
            log.warn(
                    "timeout occurred while scraping category {} on page start index {} - {}",
                    category,
                    currentStartFrom,
                    e.getMessage());
        } catch (Exception e) {
            log.error(
                    "unexpected error of class {} has occurred while scraping category {} on page start index {} - {}",
                    e.getClass(),
                    category,
                    currentStartFrom,
                    e.getMessage());
        }

        return null;
    }

    private Product createProduct(Category category, RtvEuroAgdProduct productResponse) {
        var outletPrices = productResponse
                .outletDetails()
                .map(outletDetails -> outletDetails.outletCategories().stream()
                        .map(RtvEuroAgdProduct.OutletCategory::price)
                        .toList());
        var price = getLowestPrice(productResponse.prices(), outletPrices);

        if (productResponse.eanCodes().isEmpty() || price == null) return null;

        var promoCode = productResponse
                .voucherDetails()
                .map(RtvEuroAgdProduct.VoucherDetails::voucherCode)
                .orElse(null);
        var condition = productResponse
                .outletDetails()
                .map(outletDetails -> Condition.OUTLET)
                .orElse(Condition.NEW);

        var priceStamp = new PriceStamp(price, "PLN", condition);
        priceStamp.setPromoCode(promoCode);

        var offer = new Offer(CURRENT_SHOP, getProductUrl(productResponse.identifiers()));
        offer.getPriceHistory().add(priceStamp);

        var ean = productResponse.eanCodes().getFirst();
        var images = getBigImages(productResponse.images());

        var productEntity = new Product(ean, productResponse.name(), category);
        productEntity.getImages().addAll(images);
        productEntity.getOffers().add(offer);

        return productEntity;
    }

    private BigDecimal getLowestPrice(RtvEuroAgdProduct.Prices prices, Optional<List<Long>> outletPrices) {
        var priceList = new ArrayList<Long>();

        if (prices.mainPrice() != null) priceList.add(prices.mainPrice());
        if (prices.promotionalPrice() != null)
            priceList.add(prices.promotionalPrice().price());
        if (prices.voucherDiscountedPrice() != null) priceList.add(prices.voucherDiscountedPrice());

        outletPrices.ifPresent(priceList::addAll);

        return priceList.stream()
                .filter(Objects::nonNull)
                .map(BigDecimal::new)
                .min(BigDecimal::compareTo)
                .orElse(null);
    }

    private String getProductUrl(RtvEuroAgdProduct.Identifiers identifiers) {
        var baseUrl = "https://www.euro.com.pl";
        return baseUrl + "/" + identifiers.productGroupLinkName() + "/" + identifiers.productLinkName() + ".bhtml";
    }

    private List<String> getBigImages(List<RtvEuroAgdProduct.Image> images) {
        return images.stream()
                .filter((image -> image.type().equalsIgnoreCase("BIG_PHOTO")))
                .map(RtvEuroAgdProduct.Image::url)
                .toList();
    }
}
