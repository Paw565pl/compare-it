package it.compare.backend.product.mapper;

import it.compare.backend.product.model.Product;
import it.compare.backend.product.model.Shop;
import it.compare.backend.product.response.ProductDetailResponse;
import it.compare.backend.product.response.ProductListResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMapper {
    private final ModelMapper modelMapper;

    public ProductListResponse toListResponse(Product product) {
        return modelMapper.map(product, ProductListResponse.class);
    }

    public ProductDetailResponse toDetailResponse(Product product) {
        return modelMapper.map(product, ProductDetailResponse.class);
    }

    /**
     * Maps shop enum value to human-readable name
     */
    public String mapShopNameToHumanReadable(String shopName) {
        if (shopName != null) {
            try {
                Shop shop = Shop.valueOf(shopName);
                return shop.getHumanReadableName();
            } catch (IllegalArgumentException e) {
                // return original value if not found
                return shopName;
            }
        }
        return null;
    }
}
