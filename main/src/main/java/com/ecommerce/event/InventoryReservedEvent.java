package com.ecommerce.event;

import java.time.Instant;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class InventoryReservedEvent extends Event {

    private String productId;
    private Integer quantity;

    public InventoryReservedEvent(
            String eventId,
            String productId,
            Integer quantity
    ) {
        super(
                eventId,
                "INVENTORY_RESERVED",
                Instant.now().toString()
        );

        this.productId = productId;
        this.quantity = quantity;
    }

    public String getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }
}
