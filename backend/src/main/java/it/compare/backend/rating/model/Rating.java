package it.compare.backend.rating.model;

import it.compare.backend.auth.model.User;
import java.time.LocalDateTime;
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
public class Rating {

    @DocumentReference
    @Field("author")
    private User author;

    @Field("isPositiveRating")
    @NonNull private Boolean isPositiveRating;

    @Indexed
    @CreatedDate
    @Field("createdAt")
    private LocalDateTime createdAt;
}
