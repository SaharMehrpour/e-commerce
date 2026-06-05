package com.ecommerce.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<?> handleOrderNotFoundException(
            OrderNotFoundException ex
    ) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of(
                        "timestamp", LocalDateTime.now(),
                        "error", "Order Not Found",
                        "message", ex.getMessage()
                )
        );
    }

    @ExceptionHandler(OrderAlreadyCancelledException.class)
    public ResponseEntity<?> handleOrderAlreadyCancelledException(
            OrderAlreadyCancelledException ex
    ) {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of(
                        "timestamp", LocalDateTime.now(),
                        "error", "Order Already Cancelled",
                        "message", ex.getMessage()
                )
        );
    }

    @ExceptionHandler(InvalidOrderException.class)
    public ResponseEntity<?> handleInvalidOrderException(
            InvalidOrderException ex
    ) {

        return ResponseEntity.badRequest().body(
                Map.of(
                        "timestamp", LocalDateTime.now(),
                        "error", "Invalid Order",
                        "message", ex.getMessage()
                )
        );
    }

    @ExceptionHandler(InvalidInventoryException.class)
    public ResponseEntity<?> handleInvalidInventoryException(
            InvalidInventoryException ex
    ) {

        return ResponseEntity.badRequest().body(
                Map.of(
                        "timestamp", LocalDateTime.now(),
                        "error", "Invalid Inventory Request",
                        "message", ex.getMessage()
                )
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(
            MethodArgumentNotValidException ex
    ) {
        Map<String, String> fieldErrors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.badRequest().body(
                Map.of(
                        "timestamp", LocalDateTime.now(),
                        "error", "Validation Failed",
                        "message", "Request validation failed",
                        "fields", fieldErrors
                )
        );
    }

    @ExceptionHandler(OrderNotUpdatableException.class)
    public ResponseEntity<?> handleOrderNotUpdatableException(
            OrderNotUpdatableException ex
    ) {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of(
                        "timestamp", LocalDateTime.now(),
                        "error", "Order Update Failed",
                        "message", ex.getMessage()
                )
        );
    }

    @ExceptionHandler(InventoryNotFoundException.class)
    public ResponseEntity<?> handleInventoryNotFoundException(
            InventoryNotFoundException ex
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of(
                        "timestamp", LocalDateTime.now(),
                        "error", "Inventory Not Found",
                        "message", ex.getMessage()
                )
        );
    }

    @ExceptionHandler(InventoryNotEnoughException.class)
    public ResponseEntity<?> handleInventoryNotEnoughException(
            InventoryNotEnoughException ex
    ) {
        // HTTP 422 Unprocessable Entity is perfect for business rule violations like insufficient stock
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(
                Map.of(
                        "timestamp", LocalDateTime.now(),
                        "error", "Insufficient Stock",
                        "message", ex.getMessage()
                )
        );
    }
}
