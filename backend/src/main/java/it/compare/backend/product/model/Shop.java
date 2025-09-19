package it.compare.backend.product.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Shop {
    RTV_EURO_AGD("RTV Euro AGD"),
    MEDIA_EXPERT("Media Expert"),
    MORELE_NET("Morele.net");

    private final String humanReadableName;
}
