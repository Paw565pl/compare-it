package it.compare.backend.scraping.rtveuroagd.scraper;

import generator.RandomUserAgentGenerator;
import it.compare.backend.product.model.*;
import it.compare.backend.scraping.rtveuroagd.dto.RtvEuroAgdProduct;
import it.compare.backend.scraping.rtveuroagd.dto.RtvEuroAgdResponse;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
class RtvEuroAgdScraperWorker {

    private static final Shop CURRENT_SHOP = Shop.RTV_EURO_AGD;
    private static final String BASE_URL = "https://www.euro.com.pl/rest/api/products/search";
    private static final String LOGO_URL = "https://galeriachelm.com/wp-content/uploads/2020/05/logo-web-euro.png";

    private final SecureRandom secureRandom;
    private final RestClient restClient;

    RtvEuroAgdScraperWorker(SecureRandom secureRandom, RestClient restClient) {
        this.secureRandom = secureRandom;
        this.restClient = restClient;
    }

    @Async
    public CompletableFuture<List<Product>> scrapeCategory(Category category, String categoryName) {
        final var pageSize = 25;
        var currentStartFrom = 0;

        var products = new ArrayList<Product>();

        while (true) {
            try {
                var uri = UriComponentsBuilder.fromUriString(BASE_URL)
                        .queryParam("startFrom", currentStartFrom)
                        .queryParam("numberOfItems", pageSize)
                        .queryParam("category", categoryName)
                        .queryParam("developSearchMode", "false")
                        .build()
                        .toUri();

                var productsResponse = this.restClient
                        .get()
                        .uri(uri)
                        .header("Accept", "application/json")
                        .header("User-Agent", RandomUserAgentGenerator.getNext())
                        .retrieve()
                        .body(RtvEuroAgdResponse.class);
                if (productsResponse == null
                        || productsResponse.results() == null
                        || productsResponse.results().isEmpty()) break;

                productsResponse.results().forEach(productResponse -> {
                    if (productResponse.eanCodes().isEmpty()) return;

                    var outletPrices = productResponse
                            .outletDetails()
                            .map(outletDetails -> outletDetails.outletCategories().stream()
                                    .map(RtvEuroAgdProduct.OutletCategory::price)
                                    .toList());
                    var price = getLowestPrice(productResponse.prices(), outletPrices);
                    var promoCode = productResponse
                            .voucherDetails()
                            .map(RtvEuroAgdProduct.VoucherDetails::voucherCode)
                            .orElse(null);
                    var condition = productResponse.outletDetails().isPresent() ? Condition.OUTLET : Condition.NEW;

                    var priceStamp = new PriceStamp(price, "PLN", true, condition);
                    priceStamp.setPromoCode(promoCode);

                    var offer = new Offer(CURRENT_SHOP, LOGO_URL, getProductUrl(productResponse.identifiers()));
                    offer.getPriceHistory().add(priceStamp);

                    var ean = productResponse.eanCodes().getFirst();
                    var images = getBigImages(productResponse.images());

                    var productEntity = new Product(ean, productResponse.name(), category);
                    productEntity.setImages(images);
                    productEntity.getOffers().add(offer);

                    products.add(productEntity);
                });

                currentStartFrom += pageSize;
                Thread.sleep(secureRandom.nextInt(500, 3000));
            } catch (RestClientException e) {
                log.error("Error while fetching products: {}", e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error(e.getMessage());
            }
        }

        return CompletableFuture.completedFuture(products);
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
