package it.compare.backend.product.mapper;

import it.compare.backend.product.dto.ProductListResponseDto;
import it.compare.backend.product.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {

    default ProductListResponseDto toListResponseDto(Product product) {
        return new ProductListResponseDto(
                product.getId(),
                product.getName(),
                product.getEan(),
                product.getCategory(),
                !product.getImages().isEmpty() ? product.getImages().getFirst() : null,
                product.getComputedState().getBestOffer() != null
                        ? product.getComputedState().getBestOffer().getPrice()
                        : null,
                product.getComputedState().getBestOffer() != null
                        ? product.getComputedState().getBestOffer().getCurrency()
                        : null,
                product.getComputedState().getBestOffer() != null
                        ? product.getComputedState().getBestOffer().getShop()
                        : null,
                product.getComputedState().getAvailableOffersCount(),
                product.getComputedState().getAvailableOffersCount() > 0);
    }
}
