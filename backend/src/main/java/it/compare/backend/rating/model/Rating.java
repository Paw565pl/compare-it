package it.compare.backend.rating.model;

import it.compare.backend.auth.model.User;
import it.compare.backend.comment.model.Comment;
import java.time.LocalDateTime;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
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
        def = "{'comment._id': 1, 'author._id': 1}",
        unique = true)
public class Rating {

    @MongoId
    @Field("_id")
    private String id;

    @DocumentReference
    @Field("author")
    private User author;

    @Field("isPositiveRating")
    @NonNull private Boolean isPositiveRating;

    @Indexed
    @CreatedDate
    @Field("createdAt")
    private LocalDateTime createdAt;

    @DocumentReference
    @Field("comment")
    @NonNull private Comment comment;
}
