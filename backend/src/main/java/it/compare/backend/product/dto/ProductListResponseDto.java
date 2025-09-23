package it.compare.backend.product.dto;

import it.compare.backend.product.model.Category;
import it.compare.backend.product.model.Currency;
import it.compare.backend.product.model.Shop;
import java.math.BigDecimal;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record ProductListResponseDto(
        @NonNull String id,
        @NonNull String name,
        @NonNull String ean,
        @NonNull Category category,
        @Nullable String mainImageUrl,
        @Nullable BigDecimal lowestCurrentPrice,
        @Nullable Currency lowestPriceCurrency,
        @Nullable Shop lowestPriceShop,
        @NonNull Integer availableOffersCount) {}
