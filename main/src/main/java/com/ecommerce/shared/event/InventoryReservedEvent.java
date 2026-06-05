package com.ecommerce.shared.event;

public class InventoryReservedEvent extends Event {

    private String productId;
    private Integer quantity;

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
