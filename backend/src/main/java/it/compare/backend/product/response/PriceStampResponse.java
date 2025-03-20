package it.compare.backend.product.response;

import it.compare.backend.product.model.Condition;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class PriceStampResponse {
    private LocalDateTime timestamp;
    private BigDecimal price;
    private String currency;
    private String promoCode;
    private Condition condition;
}
