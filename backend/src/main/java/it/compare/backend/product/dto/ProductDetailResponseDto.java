package it.compare.backend.product.dto;

import it.compare.backend.product.model.Category;
import it.compare.backend.product.model.Condition;
import it.compare.backend.product.model.Currency;
import it.compare.backend.product.model.Shop;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record ProductDetailResponseDto(
        @NonNull String id,
        @NonNull String ean,
        @NonNull String name,
        @NonNull Category category,
        @NonNull List<String> images,
        @NonNull List<OfferResponseDto> offers) {}

record OfferResponseDto(
        @NonNull Shop shop,
        @NonNull String url,
        boolean isAvailable,
        @NonNull List<PriceStampResponseDto> priceHistory) {}

record PriceStampResponseDto(
        @NonNull Instant timestamp,
        @NonNull BigDecimal price,
        @NonNull Currency currency,
        @Nullable String promoCode,
        @NonNull Condition condition) {}
