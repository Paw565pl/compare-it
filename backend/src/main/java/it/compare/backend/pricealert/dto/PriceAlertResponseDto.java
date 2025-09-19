package it.compare.backend.pricealert.dto;

import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record PriceAlertResponseDto(
        @NonNull String id,
        @NonNull String productId,
        @NonNull String productName,
        @NonNull BigDecimal targetPrice,
        @Nullable BigDecimal currentLowestPrice,
        @NonNull Boolean isOutletAllowed,
        @NonNull Boolean isActive,
        @NonNull Instant createdAt,
        @Nullable Instant lastNotificationSent) {}
