package it.compare.backend.pricealert.model;

import it.compare.backend.auth.model.User;
import it.compare.backend.product.model.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Getter
@Setter
@ToString
@NoArgsConstructor
@RequiredArgsConstructor
@Document(collection = "price_alerts")
public class PriceAlert {
    @MongoId
    @Field("_id")
    private String id;

    @DBRef(lazy = true)
    @Field("user")
    private User user;

    @DBRef(lazy = true)
    @Field("product")
    @NonNull private Product product;

    @Field("target_price")
    @NonNull private BigDecimal targetPrice;

    @Field("is_active")
    private boolean isActive = true;

    @Indexed
    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("last_notification_sent")
    private LocalDateTime lastNotificationSent;
}
