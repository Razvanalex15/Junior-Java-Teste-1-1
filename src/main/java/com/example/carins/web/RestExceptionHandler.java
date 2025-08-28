package com.example.carins.web;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.ServletWebRequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
public class RestExceptionHandler {

    private record ErrorBody(Instant timestamp, int status, String error, String message, String path) {}

    private ResponseEntity<ErrorBody> build(HttpStatus status, String message, ServletWebRequest req) {
        String path = req != null && req.getRequest() != null ? req.getRequest().getRequestURI() : "";
        return ResponseEntity.status(status)
                .body(new ErrorBody(Instant.now(), status.value(), status.getReasonPhrase(), message, path));
        }

    // JSON invalid / date în format greșit etc.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorBody> handleNotReadable(HttpMessageNotReadableException ex, ServletWebRequest req) {
        String msg = "Malformed JSON or invalid value. " + (ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : "");
        return build(HttpStatus.BAD_REQUEST, msg, req);
    }

    // @Valid pe @RequestBody (e.g. lipsă câmp, @NotNull, @Positive...)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorBody> handleValidation(MethodArgumentNotValidException ex, ServletWebRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, msg, req);
    }

    private String formatFieldError(FieldError fe) {
        return fe.getField() + ": " + (fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid");
    }

    // @Validated pe parametri de query/path etc.
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorBody> handleConstraintViolation(ConstraintViolationException ex, ServletWebRequest req) {
        String msg = ex.getConstraintViolations().stream()
                .map(this::formatViolation)
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, msg, req);
    }

    private String formatViolation(ConstraintViolation<?> v) {
        return v.getPropertyPath() + ": " + v.getMessage();
    }

    // Integritate DB (NOT NULL, FK, etc.) -> 400 cu mesaj clar
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorBody> handleDataIntegrity(DataIntegrityViolationException ex, ServletWebRequest req) {
        String msg = "Data integrity violation: " + (ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, msg, req);
    }

    // IllegalArgument ca 400 (de ex. startDate > endDate în PolicyController)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorBody> handleIllegalArgument(IllegalArgumentException ex, ServletWebRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }
}
