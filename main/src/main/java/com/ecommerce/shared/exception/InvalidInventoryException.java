package com.ecommerce.shared.exception;

public class InvalidInventoryException extends RuntimeException {
    public InvalidInventoryException(String message) {
        super(message);
    }
}
