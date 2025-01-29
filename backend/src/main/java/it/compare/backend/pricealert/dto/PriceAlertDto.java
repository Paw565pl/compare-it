package it.compare.backend.pricealert.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record PriceAlertDto(
        @NotNull @Positive BigDecimal targetPrice
) {}
