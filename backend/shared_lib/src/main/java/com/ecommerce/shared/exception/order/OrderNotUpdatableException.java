package com.ecommerce.shared.exception.order;

public class OrderNotUpdatableException extends RuntimeException {

    public OrderNotUpdatableException(String message) {
        super(message);
    }
}