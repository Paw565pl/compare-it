package it.compare.backend.product.response;

import it.compare.backend.product.model.Condition;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class PriceStampResponse {
    private LocalDateTime timestamp;
    private BigDecimal price;
    private String currency;

    @Nullable private String promoCode;

    private Condition condition;
}
