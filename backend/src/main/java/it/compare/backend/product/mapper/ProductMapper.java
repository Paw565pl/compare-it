package it.compare.backend.product.mapper;

import it.compare.backend.product.model.PriceStamp;
import it.compare.backend.product.model.Product;
import it.compare.backend.product.response.PriceStampResponse;
import it.compare.backend.product.response.ProductDetailResponse;
import it.compare.backend.product.response.ProductListResponse;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
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
        record LatestPrice(String shop, PriceStamp priceStamp) {}

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

        // Find the lowest current price and corresponding shop from available products
        var lowestPrice = latestPrices.stream()
                .filter(latest -> latest.priceStamp().getIsAvailable())
                .min(Comparator.comparing(latest -> latest.priceStamp().getPrice()))
                .orElse(null);

        response.setMainImageUrl(
                product.getImages().isEmpty() ? null : product.getImages().getFirst());
        response.setLowestCurrentPrice(
                lowestPrice != null ? lowestPrice.priceStamp().getPrice() : null);
        response.setLowestPriceCurrency(
                lowestPrice != null ? lowestPrice.priceStamp().getCurrency() : null);
        response.setLowestPriceShop(lowestPrice != null ? lowestPrice.shop() : null);
        response.setOfferCount(product.getOffers().stream()
                .filter(o -> o.getPriceHistory().stream().anyMatch(PriceStamp::getIsAvailable))
                .count());

        var isAvailable = lowestPrice != null && lowestPrice.priceStamp().getIsAvailable();
        response.setIsAvailable(isAvailable);

        return response;
    }

    public ProductDetailResponse toDetailResponse(Product product, Integer priceStampRangeDays) {
        var response = modelMapper.map(product, ProductDetailResponse.class);

        if (priceStampRangeDays != null) {
            LocalDateTime startDate = LocalDateTime.now().minusDays(priceStampRangeDays);
            response.getOffers().forEach(offer -> {
                List<PriceStampResponse> filteredHistory = offer.getPriceHistory().stream()
                        .filter(price -> price.getTimestamp().isAfter(startDate))
                        .toList();
                offer.setPriceHistory(filteredHistory);
            });
        }

        return response;
    }
}
