package it.compare.backend.product.dto;

import it.compare.backend.product.model.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record ProductDetailResponseDto(
        String id, String ean, String name, Category category, List<String> images, List<OfferResponseDto> offers) {}

record OfferResponseDto(@NonNull Shop shop, @NonNull String url, @NonNull List<PriceStampResponseDto> priceHistory) {

    public boolean getIsAvailable() {
        if (priceHistory.isEmpty()) return false;

        return priceHistory
                .getLast()
                .timestamp()
                .isAfter(Instant.now().minus(ComputedState.AVAILABILITY_DAYS_THRESHOLD));
    }
}

record PriceStampResponseDto(
        @NonNull Instant timestamp,
        @NonNull BigDecimal price,
        @NonNull Currency currency,
        @Nullable String promoCode,
        @NonNull Condition condition) {}
