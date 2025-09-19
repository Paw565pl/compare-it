package it.compare.backend.rating.dto;

import jakarta.validation.constraints.NotNull;

public record RatingRequestDto(@NotNull(message = "IsPositive cannot be null.") Boolean isPositive) {}
