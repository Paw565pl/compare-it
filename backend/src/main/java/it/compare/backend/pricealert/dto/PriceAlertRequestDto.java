package it.compare.backend.pricealert.dto;

import it.compare.backend.product.validator.ValidProductId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record PriceAlertRequestDto(
        @NotBlank(message = "ProductId cannot be empty.") @ValidProductId String productId,
        @NotNull(message = "TargetPrice cannot be null.") @Positive(message = "targetPrice must be greater than zero.") BigDecimal targetPrice,
        @NotNull(message = "IsOutletAllowed cannot be null.") Boolean isOutletAllowed) {}
