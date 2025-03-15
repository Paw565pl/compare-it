package it.compare.backend.scraping.morele.scraper;

import generator.RandomUserAgentGenerator;
import it.compare.backend.product.model.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class MoreleScraperWorker {
    private static final Shop CURRENT_SHOP = Shop.MORELE_NET;
    private static final String BASE_URL = "https://www.morele.net";
    private final SecureRandom secureRandom;
    private final RestClient restClient;

    public MoreleScraperWorker(SecureRandom secureRandom, RestClient restClient) {
        this.secureRandom = secureRandom;
        this.restClient = restClient;
    }

    @Async
    public CompletableFuture<List<Product>> scrapeCategory(Category category, String categoryName) {
        var products = new ArrayList<Product>();

        try {
            var initialUri = buildUri(categoryName + "/,,,,,,,,0,,,,,sprzedawca:m/1");
            var initialDocument = fetchDocument(initialUri);
            var pagesCount = getPagesCount(initialDocument);

            for (var currentPage = 1; currentPage <= pagesCount; currentPage++) {
                processCurrentPage(categoryName, currentPage, category, products);
            }
        } catch (IOException e) {
            log.error("io exception", e);
        }

        return CompletableFuture.completedFuture(products);
    }

    private void processCurrentPage(String categoryName, int currentPage, Category category, List<Product> products) {
        try {
            var currentPagePath = categoryName + "/,,,,,,,,0,,,,,sprzedawca:m/" + currentPage;
            var uri = buildUri(currentPagePath);

            var document = fetchDocument(uri);
            var productLinks = document.select("div.cat-product.card a.productLink");
            scrapeProduct(productLinks, category, products);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted error occurred", e);
        } catch (IOException | NoSuchElementException e) {
            log.error("Error occurred", e);
        }
    }

    private void scrapeProduct(Elements productLinks, Category category, List<Product> products)
            throws InterruptedException {
        for (var link : productLinks) {
            var href = BASE_URL + link.attr("href");
            try {
                var productDocument = fetchDocument(href);
                var product = createProductFromDocument(productDocument, category, href);
                if (product != null) {
                    log.info("Product created: {}", product);
                    products.add(product);
                }
                Thread.sleep(secureRandom.nextInt(500, 2000));
            } catch (HttpStatusException e) {
                handleHttpStatusException(e, href);
            } catch (NoSuchElementException | IOException e) {
                log.error("Error occurred", e);
            }
        }
    }

    private Product createProductFromDocument(Document productDocument, Category category, String href) {
        var ean = extractEan(productDocument);
        if (ean.isEmpty() || !ean.matches("\\d+")) {
            return null;
        }
        var title = extractName(productDocument);
        var price = extractPrice(productDocument);
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        var images = extractImages(productDocument);
        var condition = extractCondition(productDocument);
        var priceStamp = new PriceStamp(price, "PLN", true, condition);

        var promoCodeElement = extractPromoCode(productDocument);
        promoCodeElement.ifPresent(priceStamp::setPromoCode);

        var offer = new Offer(CURRENT_SHOP, href);
        offer.getPriceHistory().add(priceStamp);

        var productEntity = new Product(ean, title, category);
        productEntity.setImages(images);
        productEntity.getOffers().add(offer);
        return productEntity;
    }

    private void handleHttpStatusException(HttpStatusException e, String href) {
        if (e.getStatusCode() == HttpStatus.FORBIDDEN.value()) {
            log.warn("403 Forbidden error while accessing product: {}", href);
        } else {
            log.error("Error fetching product document: {}", href, e);
        }
    }

    private String buildUri(String path) {
        return UriComponentsBuilder.fromUriString(BASE_URL + path).build().toUriString();
    }

    private int getPagesCount(Document document) {
        return Integer.parseInt(
                document.select("div.pagination-btn-nolink-anchor").text().trim());
    }

    private Document fetchDocument(String uri) throws IOException {
        var response = restClient
                .get()
                .uri(uri)
                .header("User-Agent", RandomUserAgentGenerator.getNext())
                .header("Accept-Encoding", "gzip")
                .retrieve()
                .body(String.class);

        var responseBody = Optional.ofNullable(response)
                .orElseThrow(() -> new IOException("Response body is null for URI: " + uri));

        return Jsoup.parse(responseBody);
    }

    private String extractEan(Document productDocument) {
        return productDocument
                .select("div.product-specification__wrapper span.specification__value")
                .get(2)
                .text()
                .trim();
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
        var imagesList = new ArrayList<String>();
        var images = productDocument.select(
                "div.swiper-container.swiper-gallery-window div.swiper-wrapper.gallery-holder div.swiper-slide.mobx");

        for (var image : images) {
            var imageUrl = image.attr("data-src");
            if (!imageUrl.startsWith("https://www.youtube.com")) {
                imagesList.add(imageUrl);
            }
        }

        imagesList.removeIf(String::isBlank);
        return imagesList;
    }

    private Optional<String> extractPromoCode(Document productDocument) {
        var promoCodeElements =
                productDocument.select("div.product-discount-code span:not(.product-discount-code__label)");

        if (!promoCodeElements.isEmpty()) {
            return Optional.of(promoCodeElements.getFirst().text().trim());
        }
        return Optional.empty();
    }
}
