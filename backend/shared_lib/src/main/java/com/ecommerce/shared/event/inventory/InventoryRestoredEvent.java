package com.ecommerce.shared.event.inventory;

import com.ecommerce.shared.event.Event;
import com.ecommerce.shared.event.EventType;

public class InventoryRestoredEvent extends Event {

    private String productId;
    private Integer quantity;

    public InventoryRestoredEvent() {
        super(EventType.INVENTORY_RESTORED);
    }

    public InventoryRestoredEvent(
            String productId,
            Integer quantity) {
        super(
                EventType.INVENTORY_RESTORED);

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
