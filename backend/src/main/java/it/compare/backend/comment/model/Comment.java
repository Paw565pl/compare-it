package it.compare.backend.comment.model;

import it.compare.backend.auth.model.User;
import it.compare.backend.product.model.Product;
import it.compare.backend.rating.model.Rating;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
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
@Document(collection = "comments")
@CompoundIndex(
        name = "one_comment_per_product_by_user_index",
        def = "{'product.$id': 1, 'author.$id': 1}",
        partialFilter = "{ 'author': { $exists: true } }",
        unique = true)
public class Comment {

    @MongoId
    @Field("_id")
    private String id;

    @DBRef(lazy = true)
    @Field("author")
    private User author;

    @Field("text")
    @NonNull private String text;

    @Indexed
    @CreatedDate
    @Field("createdAt")
    private LocalDateTime createdAt;

    @DBRef
    @Field("product")
    @NonNull private Product product;

    @DBRef(lazy = true)
    @Field("ratings")
    private List<Rating> ratings = new ArrayList<>();
}
