package com.ecommerce.shared.event;

public class InventoryUpdatedEvent  extends Event {

    private String productId;
    private Integer availableQuantity;
    private Integer reservedQuantity;

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
