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
    public CompletableFuture<List<Product>> scrapeCategory(Category category, String categoryName) throws IOException {
        var products = new ArrayList<Product>();
        var currentPage = 1;

        String acceptLanguage = "pl-PL,pl;q=0.9,en-US;q=0.8,en;q=0.7";
        String acceptEncoding = "gzip";

        while (true) {
            var currentPagePath = categoryName + "/,,,,,,,,0,,,,,sprzedawca:m/" + currentPage;
            System.out.println("current_path: " + currentPagePath);

            var uri = UriComponentsBuilder.fromUriString(BASE_URL + currentPagePath)
                    .build()
                    .toUri();

            Document document = Jsoup.connect(uri.toString())
                    .userAgent(RandomUserAgentGenerator.getNext())
                    .header("Accept-Language", acceptLanguage)
                    .header("Accept-Encoding", acceptEncoding)
                    .get();

            var pagesCount = document.select("div.pagination-btn-nolink-anchor").text();
            System.out.println("pages_count " + pagesCount);

            var links = document.select("div.cat-product.card a.productLink");

            if (currentPage > Integer.parseInt(pagesCount)) {
                break;
            }

            try {
                for (Element link : links) {
                    var href = "https://www.morele.net" + link.attr("href");
                    Document productDocument = null;
                    try {
                        productDocument = Jsoup.connect(href)
                                .userAgent(RandomUserAgentGenerator.getNext())
                                .header("Accept-Language", acceptLanguage)
                                .header("Accept-Encoding", acceptEncoding)
                                .get();
                    } catch (HttpStatusException e) {
                        if (e.getStatusCode() == 403) {
                            log.warn("403 Forbidden error while accessing product: {}", href);
                            continue;
                        }
                        throw e;
                    }

                    try {
                        var ean = productDocument
                                .select("div.product-specification__wrapper span.specification__value")
                                .get(2)
                                .text();

                        if (ean.isEmpty() || !ean.matches("\\d{13}")) continue;

                        var title = productDocument
                                .select("h1.prod-name")
                                .getFirst()
                                .text();

                        var price = productDocument
                                .select("aside.product-sidebar div.product-box-main div.product-price")
                                .getFirst()
                                .text();
                        System.out.println("new_title: " + title + ", new_price: " + price);

                        price = price.replaceAll("[^0-9,]", "").replace(",", ".");

                        List<String> imagesList = new ArrayList<>();

                        var images = productDocument.select("div.swiper-container.swiper-gallery-thumbs img");
                        for (Element img : images) {
                            imagesList.add(img.attr("data-src"));
                        }

                        if (imagesList.isEmpty()) {
                            var image = productDocument
                                    .select("div.card-desktop prod-top-info img")
                                    .attr("src");
                            imagesList.add(image);
                        }

                        var priceStamp = new PriceStamp(new BigDecimal(price), "PLN", true, Condition.NEW);
                        var promoCodeElement = productDocument.select("div.product-discount-code span");
                        if (!promoCodeElement.isEmpty()) {
                            priceStamp.setPromoCode(promoCodeElement.last().text());
                        }
                        var offer = new Offer(CURRENT_SHOP, LOGO_URL, href);
                        offer.getPriceHistory().add(priceStamp);

                        var productEntity = new Product(ean, title, category);
                        productEntity.setImages(imagesList);
                        productEntity.getOffers().add(offer);

                        Thread.sleep(secureRandom.nextInt(2000, 5000));
                        products.add(productEntity);
                    } catch (NoSuchElementException e) {
                    }
                }

                currentPage++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return CompletableFuture.completedFuture(products);
    }
}
