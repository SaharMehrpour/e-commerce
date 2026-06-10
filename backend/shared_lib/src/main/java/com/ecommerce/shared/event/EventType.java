package com.ecommerce.shared.event;

public enum EventType {
    ORDER_CREATED,
    ORDER_CANCELLED,
    ORDER_UPDATED,

    INVENTORY_RESERVED,
    INVENTORY_FAILED,
    INVENTORY_RESTORED,
    INVENTORY_UPDATED
}