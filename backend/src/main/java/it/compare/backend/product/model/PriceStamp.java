package it.compare.backend.product.model;

import com.mongodb.lang.NonNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

@Getter
@Setter
@ToString
@NoArgsConstructor
@RequiredArgsConstructor
public class PriceStamp {
    @Field("timestamp") @CreatedDate
    private LocalDateTime timestamp;

    @NonNull @Field(value = "price", targetType = FieldType.DECIMAL128)
    private BigDecimal price;

    @NonNull @Field("currency")
    private String currency;

    @Field("promoCode")
    private String promoCode;

    @NonNull @Field("condition")
    private Condition condition;
}
