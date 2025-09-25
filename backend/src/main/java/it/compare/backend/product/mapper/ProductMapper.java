package it.compare.backend.product.mapper;

import it.compare.backend.product.dto.ProductListResponseDto;
import it.compare.backend.product.model.BestOffer;
import it.compare.backend.product.model.Product;
import java.util.Optional;
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
                Optional.ofNullable(product.getComputedState().getBestOffer())
                        .map(BestOffer::getPrice)
                        .orElse(null),
                Optional.ofNullable(product.getComputedState().getBestOffer())
                        .map(BestOffer::getCurrency)
                        .orElse(null),
                Optional.ofNullable(product.getComputedState().getBestOffer())
                        .map(BestOffer::getShop)
                        .orElse(null),
                product.getComputedState().getAvailableOffersCount());
    }
}
