package com.ecommerce.shared.event.inventory;

import com.ecommerce.shared.event.Event;
import com.ecommerce.shared.event.EventType;

public class InventoryReservedEvent extends Event {

    private String productId;
    private Integer quantity;

    public InventoryReservedEvent() {
        super(EventType.INVENTORY_RESERVED);
    }

    public InventoryReservedEvent(
            String productId,
            Integer quantity) {
        super(EventType.INVENTORY_RESERVED);

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
