package it.compare.backend.core.exception_handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        var errors = new HashMap<String, List<String>>();
        e.getFieldErrors().forEach(f -> {
            var fieldName = f.getField();
            var errorMessage = f.getDefaultMessage();

            if (errorMessage == null) return;

            if (errors.containsKey(fieldName)) {
                errors.get(fieldName).add(errorMessage);
            } else {
                errors.put(fieldName, new ArrayList<>(List.of(errorMessage)));
            }
        });

        var status = HttpStatus.BAD_REQUEST;
        var response = new ErrorResponse(status.value(), status.getReasonPhrase(), "Validation failed.", errors);

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ignored) {
        var status = HttpStatus.BAD_REQUEST;
        var response = new ErrorResponse(status.value(), status.getReasonPhrase(), "Required request body is missing.");

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(DataIntegrityViolationException e) {
        var status = HttpStatus.CONFLICT;
        var response = new ErrorResponse(status.value(), status.getReasonPhrase(), e.getMessage());

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponse> handlePropertyReferenceException(PropertyReferenceException e) {
        var status = HttpStatus.BAD_REQUEST;
        var response = new ErrorResponse(status.value(), status.getReasonPhrase(), e.getMessage());

        return ResponseEntity.status(status).body(response);
    }
}
