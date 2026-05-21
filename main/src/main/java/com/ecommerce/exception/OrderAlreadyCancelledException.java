package com.ecommerce.exception;

public class OrderAlreadyCancelledException extends RuntimeException {

    public OrderAlreadyCancelledException(String message) {
        super(message);
    }
}