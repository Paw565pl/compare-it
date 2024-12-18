package it.compare.backend.product.model;

import com.mongodb.lang.NonNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class PriceStamp {
    @NonNull @Field("timestamp")
    private LocalDateTime timestamp = LocalDateTime.now();

    @NonNull @Field("price")
    private BigDecimal price;

    @NonNull @Field("currency")
    private String currency;

    @Field("promoCode")
    private String promoCode;

    @NonNull @Field("isAvailable")
    private Boolean isAvailable;

    @NonNull @Field("condition")
    private Condition condition;
}
