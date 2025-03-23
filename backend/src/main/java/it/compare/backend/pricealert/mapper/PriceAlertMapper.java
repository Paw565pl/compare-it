package it.compare.backend.pricealert.mapper;

import it.compare.backend.pricealert.model.PriceAlert;
import it.compare.backend.pricealert.response.PriceAlertResponse;
import it.compare.backend.product.model.Condition;
import it.compare.backend.product.model.PriceStamp;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PriceAlertMapper {
    private final ModelMapper modelMapper;

    public PriceAlertResponse toResponse(PriceAlert alert) {
        var lowestCurrentPrice = alert.getProduct().getOffers().stream()
                .flatMap(offer -> offer.getPriceHistory().stream())
                .filter(priceStamp -> alert.getIsOutletAllowed() || priceStamp.getCondition() != Condition.OUTLET)
                .map(PriceStamp::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(null);

        var response = modelMapper.map(alert, PriceAlertResponse.class);
        response.setCurrentLowestPrice(lowestCurrentPrice);

        return response;
    }
}
