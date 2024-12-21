package it.compare.backend.product.response;

import it.compare.backend.product.model.Shop;
import java.util.List;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class OfferResponse {
    private Shop shop;
    private String shopLogoUrl;
    private String url;
    private List<PriceStampResponse> priceHistory;
}
