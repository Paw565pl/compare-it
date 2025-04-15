package it.compare.backend.comment.response;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class CommentResponse {

    private String id;
    private String author;
    private String text;
    private LocalDateTime createdAt;
    private Long positiveRatingsCount;
    private Long negativeRatingsCount;
    private @Nullable Boolean isRatingPositive;
}
