package it.compare.backend.rating.dto;

import jakarta.validation.constraints.NotNull;

public record RatingDto(@NotNull(message = "isPositive is required.") Boolean isPositive) {}
