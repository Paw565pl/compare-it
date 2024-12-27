package it.compare.backend.scraping.rtveuroagd.dto;

import java.util.List;

public record RtvEuroAgdResponse(Long productsCount, List<RtvEuroAgdProduct> results) {}
