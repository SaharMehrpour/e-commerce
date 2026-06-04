package com.ecommerce.event;

import java.time.Instant;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class InventoryFailedEvent extends Event {

    private String orderId;
    private String failureReason;

    public InventoryFailedEvent(
            String eventId,
            String orderId,
            String failureReason
    ) {
        super(
                eventId,
                "INVENTORY_FAILED",
                Instant.now().toString()
        );

        this.orderId = orderId;
        this.failureReason = failureReason;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getFailureReason() {
        return failureReason;
    }
}
