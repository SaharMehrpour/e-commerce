package com.ecommerce.shared.exception;

public class OrderNotUpdatableException extends RuntimeException {

    public OrderNotUpdatableException(String message) {
        super(message);
    }
}