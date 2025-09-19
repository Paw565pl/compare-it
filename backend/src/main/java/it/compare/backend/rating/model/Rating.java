package it.compare.backend.rating.model;

import it.compare.backend.auth.model.User;
import it.compare.backend.comment.model.Comment;
import java.time.Instant;
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

    @DBRef(lazy = true)
    @Field("author")
    private User author;

    @Field("isPositive")
    @NonNull private Boolean isPositive;

    @Indexed
    @CreatedDate
    @Field("createdAt")
    private Instant createdAt;

    @DBRef(lazy = true)
    @Field("comment")
    @NonNull private Comment comment;
}
