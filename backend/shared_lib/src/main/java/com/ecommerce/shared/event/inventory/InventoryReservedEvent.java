package com.ecommerce.shared.event.inventory;

import com.ecommerce.shared.event.Event;
import com.ecommerce.shared.event.EventType;

public class InventoryReservedEvent extends Event {

    private String orderId;
    private String productId;
    private Integer quantity;

    public InventoryReservedEvent() {
        super(EventType.INVENTORY_RESERVED);
    }

    public InventoryReservedEvent(
            String orderId,
            String productId,
            Integer quantity) {
        super(EventType.INVENTORY_RESERVED);

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

    public Integer getQuantity() {
        return quantity;
    }
}
