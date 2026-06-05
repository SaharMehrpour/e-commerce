package com.ecommerce.shared.event;

import java.time.Instant;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class OrderCancelledEvent extends Event {

    private String orderId;
    private String userId;
    private String productId;
    private Integer quantity;
    private String status;

    public OrderCancelledEvent(
            String eventId,
            String orderId,
            String userId,
            String productId,
            Integer quantity,
            String status
    ) {
        super(
                eventId,
                "ORDER_CANCELLED",
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

    public Integer getQuantity() {
        return quantity;
    }
    
    public String getStatus() {
        return status;
    }
}