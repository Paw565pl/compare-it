package it.compare.backend.favoriteproduct.model;

import it.compare.backend.auth.model.User;
import it.compare.backend.product.model.Product;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.*;
import org.springframework.lang.NonNull;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@Document(collection = "favorite_products")
@CompoundIndex(
        name = "unique_favorite_product_per_user_index",
        def = "{'user.$id': 1, 'product.$id': 1}",
        unique = true)
public class FavoriteProduct {

    @MongoId
    @Field(name = "_id", targetType = FieldType.OBJECT_ID)
    private String id;

    @NonNull @DBRef(lazy = true)
    @Field("user")
    private User user;

    @NonNull @DBRef(lazy = true)
    @Field("product")
    private Product product;

    @Indexed
    @CreatedDate
    @Field("createdAt")
    private Instant createdAt;
}
