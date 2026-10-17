package com.example.client.exception;

import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> status(ResponseStatusException e) {
        return build(e.getStatusCode().value(), e.getReason());
    }

    @ExceptionHandler(StorageServerException.class)
    public ResponseEntity<ErrorResponse> server(StorageServerException e) {
        return build(502, "storage-service error: " + e.getMessage());
    }

    @ExceptionHandler({CallNotPermittedException.class, RetryableException.class})
    public ResponseEntity<ErrorResponse> unavailable(Exception e) {
        return build(503, "storage-service unavailable (circuit breaker OPEN or connection problem)");
    }

    private ResponseEntity<ErrorResponse> build(int status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(status, message, LocalDateTime.now()));
    }
}