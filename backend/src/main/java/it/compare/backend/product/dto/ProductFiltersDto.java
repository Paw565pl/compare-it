package it.compare.backend.product.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductFiltersDto(
        String name, String category, List<String> shop, BigDecimal minPrice, BigDecimal maxPrice) {}
