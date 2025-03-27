package it.compare.backend.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentDto(
        @Size(min = 10, max = 2000, message = "text must be between 10 and 2000 characters long.") @NotBlank(message = "text cannot be empty.") String text) {}
