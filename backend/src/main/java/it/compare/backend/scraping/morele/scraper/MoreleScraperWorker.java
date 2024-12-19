package it.compare.backend.scraping.morele.scraper;

import generator.RandomUserAgentGenerator;
import it.compare.backend.product.model.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class MoreleScraperWorker {

    private static final Shop CURRENT_SHOP = Shop.MORELE_NET;
    private static final String BASE_URL = "https://www.morele.net";
    private static final String LOGO_URL = "https://images.morele.net/doodle/6756d1b5d579c.png";

    private final SecureRandom secureRandom;

    public MoreleScraperWorker(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    @Async
    public CompletableFuture<List<Product>> scrapeCategory(Category category, String categoryName) {
        var products = new ArrayList<Product>();
        var currentPage = 1;

        String acceptLanguage = "pl-PL,pl;q=0.9,en-US;q=0.8,en;q=0.7";
        String acceptEncoding = "gzip";

        while (true) {
            var currentPagePath = categoryName + "/,,,,,,,,0,,,,,sprzedawca:m/" + currentPage;

            var uri = buildUri(currentPagePath);

            try {
                var document = Jsoup.connect(uri)
                        .userAgent(RandomUserAgentGenerator.getNext())
                        .header("Accept-Language", acceptLanguage)
                        .header("Accept-Encoding", acceptEncoding)
                        .get();

                var pagesCount =
                        document.select("div.pagination-btn-nolink-anchor").text();

                var links = document.select("div.cat-product.card a.productLink");

                if (currentPage > Integer.parseInt(pagesCount)) {
                    break;
                }

                for (Element link : links) {
                    var href = "https://www.morele.net" + link.attr("href");
                    var productDocument = fetchProductDocument(href, acceptLanguage, acceptEncoding);
                    if (productDocument != null) {
                        Product product = parseProduct(productDocument, category);
                        if (product != null) products.add(product);
                    }
                    Thread.sleep(secureRandom.nextInt(2000, 5000));

                    currentPage++;
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return CompletableFuture.completedFuture(products);
    }

    private String buildUri(String path) {
        return UriComponentsBuilder.fromUriString(BASE_URL + path).build().toUriString();
    }

    private Document fetchProductDocument(String href, String acceptLanguage, String acceptEncoding) {
        try {
            return Jsoup.connect(href)
                    .userAgent(RandomUserAgentGenerator.getNext())
                    .header("Accept-Language", acceptLanguage)
                    .header("Accept-Encoding", acceptEncoding)
                    .get();
        } catch (HttpStatusException e) {
            if (e.getStatusCode() == 403) {
                log.warn("403 Forbidden error while accessing product: {}", href);
            } else {
                log.error("Error fetching product document: {}", href, e);
            }
        } catch (IOException e) {
            log.error("IO Exception fetching product document: {}", href, e);
        }
        return null;
    }

    private Product parseProduct(Document document, Category category) {
        try {
            var ean = document.select("div.product-specification__wrapper span.specification__value")
                    .get(2)
                    .text();
            if (ean.isEmpty() || !ean.matches("\\d{13}")) return null;

            var title = document.select("h1.prod-name").getFirst().text();

            var price = document.select("aside.product-sidebar div.product-box-main div.product-price")
                    .getFirst()
                    .text();
            price = price.replaceAll("[^0-9,]", "").replace(",", ".");

            var images = parseImages(document);

            var priceStamp = new PriceStamp(new BigDecimal(price), "PLN", true, Condition.NEW);
            var promoCodeElement = document.select("div.product-discount-code span");
            if (!promoCodeElement.isEmpty()) {
                priceStamp.setPromoCode(promoCodeElement.getLast().text());
            }

            var offer = new Offer(CURRENT_SHOP, LOGO_URL, document.location());
            offer.getPriceHistory().add(priceStamp);

            var product = new Product(ean, title, category);
            product.setImages(images);
            product.getOffers().add(offer);

            return product;
        } catch (NoSuchElementException | NullPointerException e) {
            log.error("Error parsing product", e);
        }
        return null;
    }

    private List<String> parseImages(Document document) {
        List<String> images = new ArrayList<>();
        document.select("div.swiper-container.swiper-gallery-thumbs img")
                .forEach(img -> images.add(img.attr("data-src")));
        if (images.isEmpty()) {
            var image = document.select("div.card-desktop prod-top-info img").attr("src");
            images.add(image);
        }
        return images;
    }
}
