package com.ecommerce.shared.event.inventory;

import com.ecommerce.shared.event.Event;
import com.ecommerce.shared.event.EventType;

public class InventoryFailedEvent extends Event {

    private String orderId;
    private String productId;
    private Integer quantity;

    private String failedAction;
    private String failureReason;

    public InventoryFailedEvent() {
        super(EventType.INVENTORY_FAILED);
    }

    public InventoryFailedEvent(
            String failedAction,
            String failureReason) {
        super(EventType.INVENTORY_FAILED);

        this.failedAction = failedAction;
        this.failureReason = failureReason;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getFailedAction() {
        return failedAction;
    }

    public String getFailureReason() {
        return failureReason;
    }
}
