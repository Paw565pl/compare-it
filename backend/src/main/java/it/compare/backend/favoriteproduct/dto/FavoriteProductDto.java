package it.compare.backend.favoriteproduct.dto;

import jakarta.validation.constraints.NotBlank;

public record FavoriteProductDto(@NotBlank(message = "productId is required.") String productId) {}
