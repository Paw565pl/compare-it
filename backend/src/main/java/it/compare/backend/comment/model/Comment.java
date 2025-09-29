package it.compare.backend.comment.model;

import it.compare.backend.auth.model.User;
import it.compare.backend.product.model.Product;
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
@Document(collection = "comments")
public class Comment {

    @MongoId
    @Field(name = "_id", targetType = FieldType.OBJECT_ID)
    private String id;

    @Nullable @DBRef(lazy = true)
    @Field("author")
    private User author;

    @NonNull @Field("text")
    private String text;

    @NonNull @DBRef(lazy = true)
    @Field("product")
    private Product product;

    @Indexed
    @CreatedDate
    @Field("createdAt")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updatedAt")
    private Instant updatedAt;
}
