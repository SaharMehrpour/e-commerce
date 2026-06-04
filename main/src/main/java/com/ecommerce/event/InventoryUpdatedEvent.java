package com.ecommerce.event;

import java.time.Instant;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class InventoryUpdatedEvent  extends Event {

    private String productId;
    private Integer availableQuantity;
    private Integer reservedQuantity;

    public InventoryUpdatedEvent(
            String eventId,
            String productId,
            Integer availableQuantity,
            Integer reservedQuantity
    ) {
        super(
                eventId,
                "INVENTORY_UPDATED",
                Instant.now().toString()
        );

        this.productId = productId;
        this.availableQuantity = availableQuantity;
        this.reservedQuantity = reservedQuantity;
    }

    public String getProductId() {
        return productId;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public Integer getReservedQuantity() {
        return reservedQuantity;
    }
}
