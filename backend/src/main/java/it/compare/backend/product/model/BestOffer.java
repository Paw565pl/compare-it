package it.compare.backend.product.model;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class BestOffer {

    @NonNull @Field("shop")
    private Shop shop;

    @NonNull @Field("url")
    private String url;

    @NonNull @Indexed
    @Field(name = "price", targetType = FieldType.DECIMAL128)
    private BigDecimal price;

    @NonNull @Field("currency")
    private Currency currency;

    @Nullable @Field("promoCode")
    private String promoCode;

    @NonNull @Field("condition")
    private Condition condition;
}
