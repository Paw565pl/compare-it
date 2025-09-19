package it.compare.backend.pricealert.mapper;

import it.compare.backend.pricealert.dto.PriceAlertResponseDto;
import it.compare.backend.pricealert.model.PriceAlert;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PriceAlertMapper {
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "currentLowestPrice", source = "product.computedState.bestOffer.price")
    PriceAlertResponseDto toResponse(PriceAlert priceAlert);
}
