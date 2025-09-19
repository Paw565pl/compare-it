package it.compare.backend.product.dto;

import it.compare.backend.product.model.Category;
import it.compare.backend.product.model.Shop;
import java.math.BigDecimal;
import java.util.List;

public record ProductFilterDto(
        String name,
        Category category,
        List<Shop> shops,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean isAvailable) {}
