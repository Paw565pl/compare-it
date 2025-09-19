package it.compare.backend.pricealert.model;

import it.compare.backend.auth.model.User;
import it.compare.backend.product.model.Product;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.*;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@Document(collection = "price_alerts")
public class PriceAlert {
    @MongoId
    @Field(name = "_id", targetType = FieldType.OBJECT_ID)
    private String id;

    @DBRef(lazy = true)
    @Field("user")
    private User user;

    @DBRef(lazy = true)
    @Field("product")
    @NonNull private Product product;

    @Field(name = "targetPrice", targetType = FieldType.DECIMAL128)
    @NonNull private BigDecimal targetPrice;

    @Field("isOutletAllowed")
    private Boolean isOutletAllowed;

    @Indexed
    @Field("isActive")
    private Boolean isActive = true;

    @Indexed
    @CreatedDate
    @Field("createdAt")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updatedAt")
    private Instant updatedAt;

    @Field("lastNotificationSent")
    private Instant lastNotificationSent;
}
