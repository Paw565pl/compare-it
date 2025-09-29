package it.compare.backend.rating.model;

import it.compare.backend.auth.model.User;
import it.compare.backend.comment.model.Comment;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@Document(collection = "ratings")
@CompoundIndex(
        name = "one_rating_per_comment_by_user_index",
        def = "{'comment.$id': 1, 'author.$id': 1}",
        partialFilter = "{ 'author': { $exists: true } }",
        unique = true)
public class Rating {

    @MongoId
    @Field(name = "_id", targetType = FieldType.OBJECT_ID)
    private String id;

    @Nullable @DBRef(lazy = true)
    @Field("author")
    private User author;

    @NonNull @Field("isPositive")
    private Boolean isPositive;

    @NonNull @DBRef(lazy = true)
    @Field("comment")
    private Comment comment;

    @Indexed
    @CreatedDate
    @Field("createdAt")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updatedAt")
    private Instant updatedAt;
}
