package it.compare.backend.rating.model;

import it.compare.backend.auth.model.User;
import it.compare.backend.comment.model.Comment;
import java.time.LocalDateTime;
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
@Document(collection = "ratings")
@CompoundIndex(
        name = "one_rating_per_comment_by_user_index",
        def = "{'comment.$id': 1, 'author.$id': 1}",
        partialFilter = "{ 'author': { $exists: true } }",
        unique = true)
public class Rating {

    @MongoId
    @Field("_id")
    private String id;

    @DBRef(lazy = true)
    @Field("author")
    private User author;

    @Field("isPositive")
    @NonNull private Boolean isPositive;

    @Indexed
    @CreatedDate
    @Field("createdAt")
    private LocalDateTime createdAt;

    @DBRef(lazy = true)
    @Field("comment")
    @NonNull private Comment comment;
}
