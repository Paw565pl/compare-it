package it.compare.backend.scraping.morele.dto;

import java.util.List;

public record MoreleResponse(Long productsCount, List<MoreleProduct> results) {}
