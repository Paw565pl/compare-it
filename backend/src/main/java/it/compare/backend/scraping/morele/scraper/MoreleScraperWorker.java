package it.compare.backend.scraping.morele.scraper;

import generator.RandomUserAgentGenerator;
import it.compare.backend.product.model.*;
import it.compare.backend.scraping.scraper.ScraperWorker;
import it.compare.backend.scraping.util.ScrapingUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class MoreleScraperWorker implements ScraperWorker {

    private static final Shop CURRENT_SHOP = Shop.MORELE_NET;
    private static final String BASE_URL = "https://www.morele.net";

    private final RestClient restClient;
    private final Semaphore moreleNetSemaphore;

    @Async
    @Override
    public CompletableFuture<List<Product>> scrapeCategory(Category category, String categoryLocator) {
        var isSemaphorePermitAcquired = false;

        try {
            moreleNetSemaphore.acquire();
            isSemaphorePermitAcquired = true;

            var allProducts = new ArrayList<Product>();

            var initialUri = buildUri(categoryLocator + "/,,,,,,,,0,,,,,sprzedawca:m/1");
            var initialDocumentOpt = fetchDocument(initialUri, category);
            if (initialDocumentOpt.isEmpty()) return CompletableFuture.completedFuture(allProducts);

            var pagesCount = getPagesCount(initialDocumentOpt.get());

            for (var currentPage = 1; currentPage <= pagesCount; currentPage++) {
                log.info("processing page {} for category {}", currentPage, category);
                allProducts.addAll(processCurrentPage(categoryLocator, currentPage, category));
            }

            return CompletableFuture.completedFuture(allProducts);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("thread was interrupted while acquiring semaphore for scraping category {}", category);

            return CompletableFuture.failedFuture(e);
        } finally {
            if (isSemaphorePermitAcquired) moreleNetSemaphore.release();
        }
    }

    @Override
    public Shop getShop() {
        return CURRENT_SHOP;
    }

    private List<Product> processCurrentPage(String categoryLocator, int currentPage, Category category) {
        var currentPagePath = categoryLocator + "/,,,,,,,,0,,,,,sprzedawca:m/" + currentPage;
        var uri = buildUri(currentPagePath);

        var pageProducts = new ArrayList<Product>();

        var documentOpt = fetchDocument(uri, category);
        if (documentOpt.isEmpty()) return pageProducts;

        var productLinks = documentOpt.get().select("div.cat-product.card a.productLink");
        for (var link : productLinks) {
            var product = scrapeProduct(link, category);
            if (product != null) pageProducts.add(product);

            ScrapingUtil.sleep();
        }

        return pageProducts;
    }

    private Product scrapeProduct(Element link, Category category) {
        var uri = BASE_URL + link.attr("href");

        var productDocumentOpt = fetchDocument(uri, category);
        if (productDocumentOpt.isEmpty()) return null;

        var product = createProductFromDocument(productDocumentOpt.get(), category, uri);
        if (product == null) return null;

        log.debug("product created: {}", product);
        return product;
    }

    private Product createProductFromDocument(Document productDocument, Category category, String href) {
        var ean = extractEan(productDocument);
        if (!isValidEan(ean)) return null;

        var price = extractPrice(productDocument);
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) return null;

        var title = extractName(productDocument);
        var images = extractImages(productDocument);

        var condition = extractCondition(productDocument);
        var priceStamp = new PriceStamp(price, "PLN", condition);

        var promoCodeElement = extractPromoCode(productDocument);
        promoCodeElement.ifPresent(priceStamp::setPromoCode);

        var offer = new Offer(CURRENT_SHOP, href);
        offer.getPriceHistory().add(priceStamp);

        var productEntity = new Product(ean, title, category);
        productEntity.getImages().addAll(images);
        productEntity.getOffers().add(offer);

        return productEntity;
    }

    private String buildUri(String path) {
        return UriComponentsBuilder.fromUriString(BASE_URL).path(path).build().toUriString();
    }

    private int getPagesCount(Document document) {
        return Integer.parseInt(
                document.select("div.pagination-btn-nolink-anchor").text().trim());
    }

    private Optional<Document> fetchDocument(String uri, Category category) {
        try {
            var response = restClient
                    .get()
                    .uri(uri)
                    .header("User-Agent", RandomUserAgentGenerator.getNext())
                    .header("Accept-Encoding", "gzip")
                    .retrieve()
                    .body(String.class);

            if (response == null) {
                log.warn("response body is null for uri while scraping category {}: {}", uri, category);
                return Optional.empty();
            }

            return Optional.of(Jsoup.parse(response));
        } catch (HttpStatusCodeException e) {
            log.warn(
                    "http error has occurred while scraping category {} from uri {} - {}",
                    category,
                    uri,
                    e.getStatusCode().value());
        } catch (ResourceAccessException e) {
            log.warn("timeout occurred while scraping category {} from uri {} - {}", category, uri, e.getMessage());
        } catch (Exception e) {
            log.error(
                    "unexpected error of class {} has occurred while scraping category {} from uri {} - {}",
                    e.getClass(),
                    category,
                    uri,
                    e.getMessage());
        }

        return Optional.empty();
    }

    private String extractEan(Document productDocument) {
        return productDocument.select("div.product-specification__wrapper div.specification__row").stream()
                .filter(row -> row.select("span.specification__name").text().equalsIgnoreCase("EAN"))
                .map(row -> row.select("span.specification__value").text())
                .findFirst()
                .orElse(null);
    }

    private boolean isValidEan(String ean) {
        return ean != null && !ean.isBlank() && ean.matches("\\d+");
    }

    private String extractName(Document productDocument) {
        return productDocument.select("h1.prod-name").getFirst().text();
    }

    private BigDecimal extractPrice(Document productDocument) {
        var price = Optional.ofNullable(
                productDocument.selectFirst("aside.product-sidebar div.product-box-main div.product-price"));
        if (price.isEmpty()) return null;

        var priceString = price.get().text().replaceAll("[^0-9,]", "").replace(",", ".");
        return new BigDecimal(priceString);
    }

    private Condition extractCondition(Document productDocument) {
        return productDocument.selectFirst("button.product-outlet-btn") != null ? Condition.OUTLET : Condition.NEW;
    }

    private List<String> extractImages(Document productDocument) {
        var imageList = new ArrayList<String>();
        var imageElements = productDocument.select(
                "div.swiper-container.swiper-gallery-window div.swiper-wrapper.gallery-holder div.swiper-slide.mobx");

        for (var image : imageElements) {
            var imageUrl = image.attr("data-src");
            if (!imageUrl.startsWith("https://www.youtube.com")) imageList.add(imageUrl);
        }

        imageList.removeIf(String::isBlank);

        if (imageList.isEmpty()) {
            var mainImage = productDocument.selectFirst("div.card-desktop picture img");
            if (mainImage != null) imageList.add(mainImage.attr("data-src"));
        }

        return imageList;
    }

    private Optional<String> extractPromoCode(Document productDocument) {
        var promoCodeElements =
                productDocument.select("div.product-discount-code span:not(.product-discount-code__label)");
        if (!promoCodeElements.isEmpty())
            return Optional.of(promoCodeElements.getFirst().text().trim());

        return Optional.empty();
    }
}
