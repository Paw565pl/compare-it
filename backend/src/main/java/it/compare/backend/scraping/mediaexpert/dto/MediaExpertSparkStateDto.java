package it.compare.backend.scraping.mediaexpert.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record MediaExpertSparkStateDto(
        @JsonProperty("Product:ProductShowService.state") ProductShowService productShowService) {
    public record ProductShowService(MediaExpertOffer offer) {}

    public record MediaExpertOffer(@JsonProperty("system_attributes") List<SystemAttribute> systemAttributes) {}

    public record SystemAttribute(Long id, String name, String slug, List<String> values) {}
}
