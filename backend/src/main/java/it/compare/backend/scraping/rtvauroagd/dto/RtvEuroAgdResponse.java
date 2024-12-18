package it.compare.backend.scraping.rtvauroagd.dto;

import java.util.List;

public record RtvEuroAgdResponse(Long productsCount, List<RtvEuroAgdProduct> results) {}
