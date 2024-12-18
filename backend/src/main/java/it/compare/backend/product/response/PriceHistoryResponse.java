package it.compare.backend.product.response;

import it.compare.backend.product.model.Condition;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PriceHistoryResponse {
    private LocalDateTime timestamp;
    private BigDecimal price;
    private String currency;
    private String promoCode;
    private Boolean isAvailable;
    private Condition condition;
}
