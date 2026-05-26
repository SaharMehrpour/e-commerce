package com.ecommerce.event;

import java.time.Instant;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class OrderUpdatedEvent extends Event {

    private String orderId;
    private String productId;
    private int quantity;

    public OrderUpdatedEvent(
            String eventId,
            String orderId,
            String productId,
            int quantity
    ) {
        super(
                eventId,
                "ORDER_UPDATED",
                Instant.now().toString()
        );

        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }
}