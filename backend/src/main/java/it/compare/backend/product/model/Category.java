package it.compare.backend.product.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Category {
    PROCESSORS("Procesory"),
    GRAPHICS_CARDS("Karty graficzne"),
    MOTHERBOARDS("Płyty główne"),
    RAM_MEMORY("Pamięci RAM");

    @JsonValue
    private final String humanReadableName;
}
