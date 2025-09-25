package it.compare.backend.product.model;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class PriceStamp {
    @NonNull @Field("timestamp")
    private Instant timestamp = Instant.now();

    @NonNull @Field(name = "price", targetType = FieldType.DECIMAL128)
    private BigDecimal price;

    @NonNull @Field("currency")
    private Currency currency;

    @Nullable @Field("promoCode")
    private String promoCode;

    @NonNull @Field("condition")
    private Condition condition;
}
