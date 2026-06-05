package com.ecommerce.shared.event;

public class InventoryRestoredEvent extends Event {

    private String productId;
    private Integer quantity;

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
