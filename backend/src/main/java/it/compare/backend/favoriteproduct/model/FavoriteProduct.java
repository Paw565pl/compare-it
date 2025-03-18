package it.compare.backend.favoriteproduct.model;

import it.compare.backend.auth.model.User;
import it.compare.backend.product.model.Product;
import java.time.LocalDateTime;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@RequiredArgsConstructor
@Document(collection = "favorite_products")
@CompoundIndex(
        name = "unique_favorite_product_per_user_index",
        def = "{'user.$id': 1, 'product.$id': 1}",
        unique = true)
public class FavoriteProduct {

    @MongoId
    @Field(value = "_id", targetType = FieldType.OBJECT_ID)
    private String id;

    @DBRef(lazy = true)
    @Field("user")
    @NonNull private User user;

    @DBRef(lazy = true)
    @Field("product")
    @NonNull private Product product;

    @Indexed
    @CreatedDate
    @Field("createdAt")
    private LocalDateTime createdAt;
}
