package it.compare.backend.product.mapper;

import it.compare.backend.product.model.Offer;
import it.compare.backend.product.model.PriceStamp;
import it.compare.backend.product.model.Product;
import it.compare.backend.product.response.ProductDetailResponse;
import it.compare.backend.product.response.ProductListResponse;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductListResponse toListResponse(Product product) {
        ProductListResponse response = new ProductListResponse();

        // Find latest price stamps for each offer
        record LatestPrice(String shopName, PriceStamp priceStamp) {}

        List<LatestPrice> latestPrices = product.getOffers().stream()
                .filter(offer -> !offer.getPriceHistory().isEmpty())
                .map(offer -> {
                    PriceStamp latestPrice = offer.getPriceHistory().stream()
                            .max(Comparator.comparing(PriceStamp::getTimestamp))
                            .orElse(null);
                    return new LatestPrice(offer.getShop().getHumanReadableName(), latestPrice);
                })
                .filter(latest ->
                        latest.priceStamp() != null && latest.priceStamp().getIsAvailable())
                .toList();

        // Find lowest current price and corresponding shop
        LatestPrice lowestPrice = latestPrices.stream()
                .min(Comparator.comparing(latest -> latest.priceStamp().getPrice()))
                .orElse(null);

        response.setId(product.getId());
        response.setName(product.getName());
        response.setCategory(product.getCategory());
        response.setMainImageUrl(
                product.getImages().isEmpty() ? null : product.getImages().getFirst());
        response.setLowestCurrentPrice(
                lowestPrice != null ? lowestPrice.priceStamp().getPrice() : null);
        response.setLowestPriceShopName(lowestPrice != null ? lowestPrice.shopName() : null);
        response.setOfferCount((int) product.getOffers().stream()
                .filter(o -> o.getPriceHistory().stream().anyMatch(PriceStamp::getIsAvailable))
                .count());
        response.setIsAvailable(!latestPrices.isEmpty());

        return response;
    }

    public ProductDetailResponse toDetailResponse(Product product) {
        ProductDetailResponse response = new ProductDetailResponse();

        response.setId(product.getId());
        response.setEan(product.getEan());
        response.setName(product.getName());
        response.setCategory(product.getCategory());
        response.setImages(product.getImages());
        response.setOffers(
                product.getOffers().stream().map(this::toOfferResponse).toList());

        return response;
    }

    private ProductDetailResponse.OfferResponse toOfferResponse(Offer offer) {
        ProductDetailResponse.OfferResponse response = new ProductDetailResponse.OfferResponse();

        response.setShop(offer.getShop().getHumanReadableName());
        response.setShopLogoUrl(offer.getShopLogoUrl());
        response.setUrl(offer.getUrl());
        response.setPriceHistory(offer.getPriceHistory().stream()
                .map(this::toPriceHistoryResponse)
                .toList());

        return response;
    }

    private ProductDetailResponse.PriceHistoryResponse toPriceHistoryResponse(PriceStamp priceStamp) {
        ProductDetailResponse.PriceHistoryResponse response = new ProductDetailResponse.PriceHistoryResponse();

        response.setTimestamp(priceStamp.getTimestamp());
        response.setPrice(priceStamp.getPrice());
        response.setCurrency(priceStamp.getCurrency());
        response.setPromoCode(priceStamp.getPromoCode());
        response.setIsAvailable(priceStamp.getIsAvailable());
        response.setCondition(priceStamp.getCondition());

        return response;
    }
}
