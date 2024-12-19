package it.compare.backend.product.mapper;

import it.compare.backend.product.model.PriceStamp;
import it.compare.backend.product.model.Product;
import it.compare.backend.product.response.ProductDetailResponse;
import it.compare.backend.product.response.ProductListResponse;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMapper {
    private final ModelMapper modelMapper;

    public ProductListResponse toListResponse(Product product) {
        var response = modelMapper.map(product, ProductListResponse.class);

        // Find latest price stamps for each offer
        record LatestPrice(String shopName, PriceStamp priceStamp) {}

        var latestPrices = product.getOffers().stream()
                .filter(offer -> !offer.getPriceHistory().isEmpty())
                .map(offer -> {
                    var latestPrice = offer.getPriceHistory().stream()
                            .max(Comparator.comparing(PriceStamp::getTimestamp))
                            .orElse(null);
                    return new LatestPrice(offer.getShop().getHumanReadableName(), latestPrice);
                })
                .filter(latest -> latest.priceStamp() != null)
                .toList();

        // Find lowest current price and corresponding shop from available products
        var lowestPrice = latestPrices.stream()
                .filter(latest -> latest.priceStamp().getIsAvailable())
                .min(Comparator.comparing(latest -> latest.priceStamp().getPrice()))
                .orElse(null);

        response.setMainImageUrl(
                product.getImages().isEmpty() ? null : product.getImages().getFirst());
        response.setLowestCurrentPrice(
                lowestPrice != null ? lowestPrice.priceStamp().getPrice() : null);
        response.setLowestPriceShop(lowestPrice != null ? lowestPrice.shopName() : null);
        response.setOfferCount(Math.toIntExact(product.getOffers().stream()
                .filter(o -> o.getPriceHistory().stream().anyMatch(PriceStamp::getIsAvailable))
                .count()));

        var isAvailable = lowestPrice != null && lowestPrice.priceStamp().getIsAvailable();
        response.setIsAvailable(isAvailable);

        return response;
    }

    public ProductDetailResponse toDetailResponse(Product product) {
        return modelMapper.map(product, ProductDetailResponse.class);
    }
}
