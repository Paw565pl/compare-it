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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class MoreleScraperWorker {
    private static final Shop CURRENT_SHOP = Shop.MORELE_NET;
    private static final String BASE_URL = "https://www.morele.net";
    private static final String LOGO_URL = "https://www.morele.net/assets/src/images/socials/morele_logo_fb.png";
    private final SecureRandom secureRandom;

    public MoreleScraperWorker(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    @Async
    public CompletableFuture<List<Product>> scrapeCategory(Category category, String categoryName) {
        var products = new ArrayList<Product>();
        var acceptLanguage = "pl-PL,pl;q=0.9,en-US;q=0.8,en;q=0.7";
        var acceptEncoding = "gzip";

        try {
            var initialUri = buildUri(categoryName + "/,,,,,,,,0,,,,,sprzedawca:m/1");
            var initialDocument = fetchDocument(initialUri, acceptLanguage, acceptEncoding);
            var pagesCount = getPagesCount(initialDocument);

            for (var currentPage = 1; currentPage <= pagesCount; currentPage++) {
                processCurrentPage(categoryName, currentPage, acceptLanguage, acceptEncoding, category, products);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        return CompletableFuture.completedFuture(products);
    }

    private void processCurrentPage(
            String categoryName,
            int currentPage,
            String acceptLanguage,
            String acceptEncoding,
            Category category,
            List<Product> products) {
        try {
            var currentPagePath = categoryName + "/,,,,,,,,0,,,,,sprzedawca:m/" + currentPage;
            var uri = buildUri(currentPagePath);

            var document = fetchDocument(uri, acceptLanguage, acceptEncoding);
            var productLinks = document.select("div.cat-product.card a.productLink");
            scrapeProduct(productLinks, category, acceptLanguage, acceptEncoding, products);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(e.getMessage());
        } catch (IOException | NoSuchElementException e) {
            log.error(e.getMessage());
        }
    }

    private void scrapeProduct(
            Elements productLinks,
            Category category,
            String acceptLanguage,
            String acceptEncoding,
            List<Product> products)
            throws InterruptedException {
        for (var link : productLinks) {
            var href = BASE_URL + link.attr("href");
            try {
                var productDocument = fetchProductDocument(href, acceptLanguage, acceptEncoding);
                var product = createProductFromDocument(productDocument, category, href);
                if (product != null) {
                    products.add(product);
                }
                Thread.sleep(secureRandom.nextInt(2000, 5000));
            } catch (HttpStatusException e) {
                handleHttpStatusException(e, href);
            } catch (NoSuchElementException | IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    private Product createProductFromDocument(Document productDocument, Category category, String href) {
        var ean = extractEan(productDocument);
        if (ean.isEmpty() || !ean.matches("\\d{13}")) {
            return null;
        }

        var title = extractName(productDocument);
        var price = extractPrice(productDocument);
        var images = extractImages(productDocument);
        var priceStamp = new PriceStamp(price, "PLN", true, Condition.NEW);

        var promoCodeElement = extractPromoCode(productDocument);
        promoCodeElement.ifPresent(
                elements -> priceStamp.setPromoCode(elements.getLast().text()));

        var offer = new Offer(CURRENT_SHOP, LOGO_URL, href);
        offer.getPriceHistory().add(priceStamp);

        var productEntity = new Product(ean, title, category);
        productEntity.setImages(images);
        productEntity.getOffers().add(offer);

        return productEntity;
    }

    private Document fetchProductDocument(String href, String acceptLanguage, String acceptEncoding)
            throws IOException {
        return Jsoup.connect(href)
                .userAgent(RandomUserAgentGenerator.getNext())
                .header("Accept-Language", acceptLanguage)
                .header("Accept-Encoding", acceptEncoding)
                .get();
    }

    private void handleHttpStatusException(HttpStatusException e, String href) {
        if (e.getStatusCode() == 403) {
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
                document.select("div.pagination-btn-nolink-anchor").text());
    }

    private Document fetchDocument(String uri, String acceptLanguage, String acceptEncoding) throws IOException {
        return Jsoup.connect(uri)
                .userAgent(RandomUserAgentGenerator.getNext())
                .header("Accept-Language", acceptLanguage)
                .header("Accept-Encoding", acceptEncoding)
                .get();
    }

    private String extractEan(Document productDocument) {
        return productDocument
                .select("div.product-specification__wrapper span.specification__value")
                .get(2)
                .text();
    }

    private String extractName(Document productDocument) {
        return productDocument.select("h1.prod-name").getFirst().text();
    }

    private BigDecimal extractPrice(Document productDocument) {
        var price = productDocument
                .select("aside.product-sidebar div.product-box-main div.product-price")
                .getFirst()
                .text();
        price = price.replaceAll("[^0-9,]", "").replace(",", ".");
        return new BigDecimal(price);
    }

    private List<String> extractImages(Document productDocument) {
        var imagesList = new ArrayList<String>();
        var images = productDocument.select("div.swiper-container.swiper-gallery-thumbs img");
        for (var image : images) {
            imagesList.add(image.attr("data-src"));
        }
        if (imagesList.isEmpty()) {
            var image =
                    productDocument.select("div.card-desktop prod-top-info img").attr("src");
            imagesList.add(image);
        }

        imagesList.removeIf(String::isEmpty);
        return imagesList;
    }

    private Optional<Elements> extractPromoCode(Document productDocument) {
        return Optional.of(productDocument.select("div.product-discount-code span"));
    }
}
