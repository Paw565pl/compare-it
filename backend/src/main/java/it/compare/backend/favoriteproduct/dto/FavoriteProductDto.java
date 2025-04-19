package it.compare.backend.favoriteproduct.dto;

import it.compare.backend.product.validator.ValidProductId;

public record FavoriteProductDto(@ValidProductId String productId) {}
