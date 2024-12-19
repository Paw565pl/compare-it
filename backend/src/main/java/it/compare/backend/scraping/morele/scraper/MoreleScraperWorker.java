package it.compare.backend.scraping.morele.scraper;

import generator.RandomUserAgentGenerator;
import it.compare.backend.product.model.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
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
    private static final String BASE_URL = "https://www.morele.net/wyszukiwarka";
    private static final String LOGO_URL =
            "https://www.morele.net/assets/img/logos/logo-2.png"; // Adjust the logo URL as needed

    private final SecureRandom secureRandom;

    public MoreleScraperWorker(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    @Async
    public CompletableFuture<List<Product>> scrapeCategory(Category category, String categoryName) throws IOException {
        final var pageSize = 25;
        var currentStartFrom = 0;

        var products = new ArrayList<Product>();

        var uri = UriComponentsBuilder.fromUriString(BASE_URL)
                .queryParam("q", categoryName) // Use the query parameter for the category
                .build()
                .toUri();

        String acceptLanguage = "pl-PL,pl;q=0.9,en-US;q=0.8,en;q=0.7";
        String acceptEncoding = "gzip";
        System.out.println("Uri: " + uri);

        Document document = Jsoup.connect(uri.toString())
                .userAgent(RandomUserAgentGenerator.getNext())
                .header("Accept-Language", acceptLanguage)
                .header("Accept-Encoding", acceptEncoding)
                .get();

        var links = document.select("div.cat-product.card a.productLink");

        try {
            for (Element link : links) {
                var href = "https://www.morele.net" + link.attr("href");
                Document productDocument = Jsoup.connect(href)
                        .userAgent(RandomUserAgentGenerator.getNext())
                        .header("Accept-Language", acceptLanguage)
                        .header("Accept-Encoding", acceptEncoding)
                        .get();

                var title = productDocument.select("h1.prod-name").getFirst().text();
                System.out.println(title);

                var price = productDocument
                        .select("aside.product-sidebar div.product-box-main div.product-price")
                        .getFirst()
                        .text();
                price = price.replaceAll("[^0-9,]", "").replace(",", ".");
                System.out.println(price);

                var ean = productDocument
                        .select("div.product-specification__wrapper span.specification__value")
                        .get(2)
                        .text();
                System.out.println(ean);

                var productEntity = new Product(ean, title, category);
                var priceStamp = new PriceStamp(new BigDecimal(price), "PLN", true, Condition.NEW);
                var offer = new Offer(CURRENT_SHOP, LOGO_URL, href);
                offer.getPriceHistory().add(priceStamp);
                Thread.sleep(secureRandom.nextInt(500, 3000));
                products.add(productEntity);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(e.getMessage());
        }
        return CompletableFuture.completedFuture(products);
    }
}
