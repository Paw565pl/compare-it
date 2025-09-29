package it.compare.backend.comment.dto;

import java.time.Instant;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record CommentResponseDto(
        @NonNull String id,
        @Nullable String author,
        @NonNull String text,
        @NonNull Instant createdAt,
        long positiveRatingsCount,
        long negativeRatingsCount,
        @Nullable Boolean isRatingPositive) {}
