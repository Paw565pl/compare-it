package it.compare.backend.rating.dto;

import jakarta.validation.constraints.NotNull;

public record RatingDto(@NotNull(message = "isPositive cannot be empty.") Boolean isPositive) {}
