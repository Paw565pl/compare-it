package it.compare.backend.product.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Condition {
    NEW("Nowy"),
    OUTLET("Outlet");

    @JsonValue
    private final String humanReadableName;
}
