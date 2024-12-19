package it.compare.backend.product.response;

import java.util.List;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class OfferResponse {
    private String shop;
    private String shopLogoUrl;
    private String url;
    private List<PriceStampResponse> priceHistory;
}
