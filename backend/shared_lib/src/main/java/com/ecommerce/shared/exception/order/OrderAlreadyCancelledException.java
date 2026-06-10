package com.ecommerce.shared.exception.order;

public class OrderAlreadyCancelledException extends RuntimeException {

    public OrderAlreadyCancelledException(String message) {
        super(message);
    }
}