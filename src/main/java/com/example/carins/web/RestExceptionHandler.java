package com.example.carins.web;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestControllerAdvice
public class RestExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String,Object>> handleValidation(MethodArgumentNotValidException ex) {
    Map<String,Object> body = new LinkedHashMap<>();
    body.put("status", 400);
    body.put("error", "Bad Request");
    body.put("messages", ex.getBindingResult().getFieldErrors().stream()
        .map(fe -> fe.getField() + ": " + fe.getDefaultMessage()).toList());
    return ResponseEntity.badRequest().body(body);
  }
}
