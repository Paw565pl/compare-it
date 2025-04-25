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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public CompletableFuture<List<Product>> scrapeCategory(Category category, String categoryLocator) {
        final var PAGE_SIZE = 25;
        var currentStartFrom = 0;

        var products = new ArrayList<Product>();

        while (true) {
            try {
                var uri = UriComponentsBuilder.fromUriString(BASE_URL)
                        .queryParam("numberOfItems", PAGE_SIZE)
                        .queryParam("startFrom", currentStartFrom)
                        .queryParam("category", categoryLocator)
                        .queryParam("developSearchMode", "false")
                        .build()
                        .toUri();

                var productResponses = this.restClient
                        .get()
                        .uri(uri)
                        .header("Accept", "application/json")
                        .header("User-Agent", RandomUserAgentGenerator.getNext())
                        .retrieve()
                        .body(RtvEuroAgdResponse.class);
                if (productResponses == null
                        || productResponses.results() == null
                        || productResponses.results().isEmpty()) break;

                productResponses.results().forEach(productResponse -> {
                    var outletPrices = productResponse
                            .outletDetails()
                            .map(outletDetails -> outletDetails.outletCategories().stream()
                                    .map(RtvEuroAgdProduct.OutletCategory::price)
                                    .toList());
                    var price = getLowestPrice(productResponse.prices(), outletPrices);

                    if (productResponse.eanCodes().isEmpty() || price == null) return;

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
                    productEntity.setImages(images);
                    productEntity.getOffers().add(offer);

                    products.add(productEntity);
                });

                currentStartFrom += PAGE_SIZE;
                ScrapingUtil.sleep();
            } catch (HttpStatusCodeException e) {
                log.warn(
                        "http error has occurred while scraping products from category {} - {}",
                        category,
                        e.getStatusCode().value());
            } catch (ResourceAccessException e) {
                log.warn(
                        "timeout occurred in Rtv Euro Agd scraper while scraping category {} - {}",
                        category,
                        e.getMessage());
            } catch (Exception e) {
                log.error(
                        "unexpected error of class {} has occurred while scraping products from category {} - {}",
                        e.getClass(),
                        category,
                        e.getMessage());
            }
        }

        return CompletableFuture.completedFuture(products);
    }

    @Override
    public Shop getShop() {
        return CURRENT_SHOP;
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
