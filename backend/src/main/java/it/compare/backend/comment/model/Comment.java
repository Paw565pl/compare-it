package it.compare.backend.comment.model;

import it.compare.backend.auth.model.User;
import it.compare.backend.rating.model.Rating;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@ToString
@NoArgsConstructor
@RequiredArgsConstructor
public class Comment {

    @DocumentReference
    @Field("author")
    private User author;

    @Field("text")
    @NonNull private String text;

    @Indexed
    @CreatedDate
    @Field("createdAt")
    private LocalDateTime createdAt;

    @Field("ratings")
    private List<Rating> ratings = new ArrayList<>();
}
