package it.compare.backend.product.dto;

import java.math.BigDecimal;

public record ProductFiltersDto(String name, String category, String shop, BigDecimal minPrice, BigDecimal maxPrice) {}
