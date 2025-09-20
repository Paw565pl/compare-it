package it.compare.backend.scraping.rtveuroagd.dto;

import java.util.List;

public record RtvEuroAgdResponse(Integer productsCount, List<RtvEuroAgdProduct> results) {}
