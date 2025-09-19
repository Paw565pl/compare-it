package it.compare.backend.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequestDto(
        @Size(min = 10, max = 2000, message = "Text must be between 10 and 2000 characters long.") @NotBlank(message = "Text cannot be empty.") String text) {}
