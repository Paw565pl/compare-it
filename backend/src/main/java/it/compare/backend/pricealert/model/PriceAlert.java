package it.compare.backend.pricealert.model;

import it.compare.backend.auth.model.User;
import it.compare.backend.product.model.Product;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@Document(collection = "price_alerts")
public class PriceAlert {
    @MongoId
    @Field(name = "_id", targetType = FieldType.OBJECT_ID)
    private String id;

    @NonNull @DBRef(lazy = true)
    @Field("user")
    private User user;

    @NonNull @DBRef(lazy = true)
    @Field("product")
    private Product product;

    @NonNull @Field(name = "targetPrice", targetType = FieldType.DECIMAL128)
    private BigDecimal targetPrice;

    @NonNull @Field("isOutletAllowed")
    private Boolean isOutletAllowed;

    @NonNull @Indexed
    @Field("isActive")
    private Boolean isActive = true;

    @Nullable @Field("lastNotificationSent")
    private Instant lastNotificationSent;

    @Indexed
    @CreatedDate
    @Field("createdAt")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updatedAt")
    private Instant updatedAt;
}
