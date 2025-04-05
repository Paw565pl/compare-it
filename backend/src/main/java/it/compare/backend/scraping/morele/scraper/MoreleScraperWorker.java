package it.compare.backend.scraping.morele.scraper;

import generator.RandomUserAgentGenerator;
import it.compare.backend.product.model.*;
import it.compare.backend.scraping.util.ScrapingUtil;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class MoreleScraperWorker {

    private static final Shop CURRENT_SHOP = Shop.MORELE_NET;
    private static final String BASE_URL = "https://www.morele.net";

    private final RestClient restClient;

    @Async
    public CompletableFuture<List<Product>> scrapeCategory(Category category, String categoryName) {
        var products = new ArrayList<Product>();

        try {
            var initialUri = buildUri(categoryName + "/,,,,,,,,0,,,,,sprzedawca:m/1");
            var initialDocument = fetchDocument(initialUri);
            var pagesCount = getPagesCount(initialDocument);

            for (var currentPage = 1; currentPage <= pagesCount; currentPage++) {
                log.info("processing page {} for category {}", currentPage, category);
                processCurrentPage(categoryName, currentPage, category, products);
            }
        } catch (Exception e) {
            log.error("unexpected error has occurred while scraping category - {}", e.getMessage());
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
        } catch (IOException | NoSuchElementException e) {
            log.error("Error occurred", e);
        } catch (Exception e) {
            log.error("unexpected error has occurred while scraping category - {}", e.getMessage());
        }
    }

    private void scrapeProduct(Elements productLinks, Category category, List<Product> products) {
        for (var link : productLinks) {
            var href = BASE_URL + link.attr("href");

            try {
                var productDocument = fetchDocument(href);
                var product = createProductFromDocument(productDocument, category, href);

                if (product != null) {
                    log.debug("product created: {}", product);
                    products.add(product);
                }

                ScrapingUtil.sleep();
            } catch (HttpStatusException e) {
                handleHttpStatusException(e, href);
            } catch (Exception e) {
                log.error(
                        "unexpected error has occurred while scraping category at href {} - {}", href, e.getMessage());
            }
        }
    }

    private Product createProductFromDocument(Document productDocument, Category category, String href) {
        var ean = extractEan(productDocument);
        if (ean.isEmpty() || !ean.matches("\\d+")) return null;

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

    private void handleHttpStatusException(HttpStatusException e, String url) {
        if (e.getStatusCode() == HttpStatus.FORBIDDEN.value()) {
            log.warn("403 forbidden error while accessing product at uri {} - {}", url, e.getMessage());
        } else {
            log.error("error while fetching product document at uri {} - {}", url, e.getMessage());
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
                .orElseThrow(() -> new IOException("response body is null for URI: " + uri));

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
        var imageList = new ArrayList<String>();
        var imageElements = productDocument.select(
                "div.swiper-container.swiper-gallery-window div.swiper-wrapper.gallery-holder div.swiper-slide.mobx");

        for (var image : imageElements) {
            var imageUrl = image.attr("data-src");
            if (!imageUrl.startsWith("https://www.youtube.com")) imageList.add(imageUrl);
        }

        imageList.removeIf(String::isBlank);
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
