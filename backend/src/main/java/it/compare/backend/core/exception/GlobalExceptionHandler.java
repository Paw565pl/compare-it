package it.compare.backend.core.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        var errors = e.getFieldErrors().stream()
                .filter(fieldError -> fieldError.getDefaultMessage() != null
                        && !fieldError.getDefaultMessage().isBlank())
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(DefaultMessageSourceResolvable::getDefaultMessage, Collectors.toList())));

        var status = HttpStatus.BAD_REQUEST;
        var response = new ErrorResponseDto(status.value(), status.getReasonPhrase(), "Validation failed.", errors);

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolationException(ConstraintViolationException e) {
        var errors = e.getConstraintViolations().stream()
                .map(error -> {
                    var propertyPathIterator = error.getPropertyPath().iterator();

                    String fieldName = "";
                    while (propertyPathIterator.hasNext()) {
                        fieldName = propertyPathIterator.next().getName();
                    }

                    return Map.entry(fieldName, error.getMessage());
                })
                .filter(entry -> !entry.getKey().isBlank()
                        && entry.getValue() != null
                        && !entry.getValue().isBlank())
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

        var status = HttpStatus.BAD_REQUEST;
        var response = new ErrorResponseDto(status.value(), status.getReasonPhrase(), "Validation failed.", errors);

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ignored) {
        var status = HttpStatus.BAD_REQUEST;
        var response =
                new ErrorResponseDto(status.value(), status.getReasonPhrase(), "Required request body is missing.");

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(DataIntegrityViolationException e) {
        var status = HttpStatus.CONFLICT;
        var response = new ErrorResponseDto(status.value(), status.getReasonPhrase(), e.getMessage());

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponseDto> handlePropertyReferenceException(PropertyReferenceException e) {
        var status = HttpStatus.BAD_REQUEST;
        var response = new ErrorResponseDto(status.value(), status.getReasonPhrase(), e.getMessage());

        return ResponseEntity.status(status).body(response);
    }
}
