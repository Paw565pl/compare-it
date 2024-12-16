package it.compare.backend.product.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
public enum Shop {
    RTV_EURO_AGD("RTV Euro AGD"),
    MEDIA_EXPERT("Media Expert"),
    MORELE_NET("Morele.net");

    @JsonValue
    private final String humanReadableName;
}
