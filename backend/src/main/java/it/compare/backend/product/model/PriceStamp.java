package it.compare.backend.product.model;

import com.mongodb.lang.NonNull;
import com.mongodb.lang.Nullable;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

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
