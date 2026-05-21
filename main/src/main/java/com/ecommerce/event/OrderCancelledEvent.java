package com.ecommerce.event;

import java.time.Instant;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class OrderCancelledEvent extends Event {

    private String orderId;
    private String userId;
    private String status;

    public OrderCancelledEvent(
            String eventId,
            String orderId,
            String userId,
            String status
    ) {
        super(
                eventId,
                "ORDER_CANCELLED",
                Instant.now().toString()
        );

        this.orderId = orderId;
        this.userId = userId;
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getUserId() {
        return userId;
    }

    public String getStatus() {
        return status;
    }
}