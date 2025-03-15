package it.compare.backend.product.mapper;

import it.compare.backend.product.model.Product;
import it.compare.backend.product.model.Shop;
import it.compare.backend.product.response.PriceStampResponse;
import it.compare.backend.product.response.ProductDetailResponse;
import it.compare.backend.product.response.ProductListResponse;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMapper {
    private final ModelMapper modelMapper;

    public ProductListResponse toListResponse(Product product) {
        return modelMapper.map(product, ProductListResponse.class);
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

    public Page<ProductListResponse> mapShopNames(
            List<ProductListResponse> productResponses, Pageable pageable, long total) {
        productResponses.forEach(this::mapShopName);
        return new PageImpl<>(productResponses, pageable, total);
    }

    public void mapShopName(ProductListResponse response) {
        if (response.getLowestPriceShop() != null) {
            try {
                Shop shop = Shop.valueOf(response.getLowestPriceShop());
                response.setLowestPriceShop(shop.getHumanReadableName());
            } catch (IllegalArgumentException e) {
                // return original value if not found
            }
        }
    }
}
