package it.compare.backend.core.exception;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude(NON_NULL)
public class ErrorResponseDto {

    private Instant timestamp;
    private Integer status;
    private String error;
    private String message;
    private Map<String, List<String>> errors;

    public ErrorResponseDto(Integer status, String error, String message, Map<String, List<String>> errors) {
        this.timestamp = Instant.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.errors = errors;
    }

    public ErrorResponseDto(Integer status, String error, String message) {
        this.timestamp = Instant.now();
        this.status = status;
        this.error = error;
        this.message = message;
    }
}
