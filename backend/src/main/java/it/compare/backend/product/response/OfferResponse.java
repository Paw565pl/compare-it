package it.compare.backend.product.response;

import java.util.List;
import lombok.Data;

@Data
public class OfferResponse {
    private String shop;
    private String shopLogoUrl;
    private String url;
    private List<PriceHistoryResponse> priceHistory;
}
