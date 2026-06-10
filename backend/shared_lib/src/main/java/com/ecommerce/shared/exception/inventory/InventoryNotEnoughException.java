package com.ecommerce.shared.exception.inventory;

public class InventoryNotEnoughException extends RuntimeException {
    public InventoryNotEnoughException(String message) {
        super(message);
    }
}
