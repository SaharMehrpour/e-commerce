package com.ecommerce.shared.event.inventory;

import com.ecommerce.shared.event.Event;
import com.ecommerce.shared.event.EventType;

public class InventoryUpdatedEvent  extends Event {

    private String productId;
    private Integer availableQuantity;
    private Integer reservedQuantity;

    public InventoryUpdatedEvent() {
        super(EventType.INVENTORY_UPDATED);
    }

    public InventoryUpdatedEvent(
            String productId,
            Integer availableQuantity,
            Integer reservedQuantity) {
        super(EventType.INVENTORY_UPDATED);

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
