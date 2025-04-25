package it.compare.backend.favoriteproduct.dto;

import it.compare.backend.product.validator.ValidProductId;
import jakarta.validation.constraints.NotBlank;

public record FavoriteProductDto(@NotBlank(message = "productId cannot be empty.") @ValidProductId String productId) {}
