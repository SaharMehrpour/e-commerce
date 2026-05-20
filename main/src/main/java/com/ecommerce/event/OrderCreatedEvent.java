package com.ecommerce.event;

import java.time.Instant;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class OrderCreatedEvent extends Event {

    private String orderId;
    private String userId;
    private String productId;
    private int quantity;
    private String status;

    public OrderCreatedEvent(
            String eventId,
            String orderId,
            String userId,
            String productId,
            int quantity,
            String status
    ) {
        super(
                eventId,
                "ORDER_CREATED",
                Instant.now().toString()
        );

        this.orderId = orderId;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getUserId() {
        return userId;
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getStatus() {
        return status;
    }
}