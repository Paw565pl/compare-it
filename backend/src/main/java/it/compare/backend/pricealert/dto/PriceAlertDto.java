package it.compare.backend.pricealert.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record PriceAlertDto(
        @NotBlank(message = "productId cannot be empty.") String productId,
        @NotNull(message = "targetPrice cannot be null.") @Positive(message = "targetPrice must be greater than zero.") BigDecimal targetPrice,
        @NotNull(message = "isOutletAllowed cannot be null.") Boolean isOutletAllowed) {}
