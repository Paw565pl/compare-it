package it.compare.backend.comment.model;

import it.compare.backend.auth.model.User;
import it.compare.backend.product.model.Product;
import java.time.Instant;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@RequiredArgsConstructor
@Document(collection = "comments")
public class Comment {

    @MongoId
    @Field(name = "_id", targetType = FieldType.OBJECT_ID)
    private String id;

    @DBRef(lazy = true)
    @Field("author")
    private User author;

    @Field("text")
    @NonNull private String text;

    @Indexed
    @CreatedDate
    @Field("createdAt")
    private Instant createdAt;

    @DBRef(lazy = true)
    @Field("product")
    @NonNull private Product product;
}
