package it.compare.backend.product.response;

import it.compare.backend.product.model.Category;
import it.compare.backend.product.model.Condition;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class ProductDetailResponse {
    private String id;
    private String ean;
    private String name;
    private Category category;
    private List<String> images;
    private List<OfferResponse> offers;

    @Data
    public static class OfferResponse {
        private String shopName;
        private String shopLogoUrl;
        private String url;
        private List<PriceHistoryResponse> priceHistory;
    }

    @Data
    public static class PriceHistoryResponse {
        private LocalDateTime timestamp;
        private BigDecimal price;
        private String currency;
        private String promoCode;
        private boolean available;
        private Condition condition;
    }
}
