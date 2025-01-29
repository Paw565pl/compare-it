package it.compare.backend.pricealert.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record PriceAlertDto(
        @NotBlank(message = "Product ID cannot be empty")
        String productId,

        @NotNull(message = "Target price cannot be null")
        @Positive(message = "Target price must be greater than zero")
        BigDecimal targetPrice
) {}
