package it.compare.backend.pricealert.dto;

import it.compare.backend.product.validator.ValidProductId;

public record PriceAlertFiltersDto(@ValidProductId String productId, Boolean isActive) {}
