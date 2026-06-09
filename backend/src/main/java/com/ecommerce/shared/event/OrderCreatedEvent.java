package com.ecommerce.shared.event;

import com.ecommerce.order.domain.OrderStatus;

public class OrderCreatedEvent extends Event {

    private String orderId;
    private String userId;
    private String productId;
    private int quantity;
    private OrderStatus status;

    public OrderCreatedEvent(
            String orderId,
            String userId,
            String productId,
            int quantity,
            OrderStatus status) {
        super(EventType.ORDER_CREATED);
        this.orderId = orderId;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getUserId() {
        return userId;
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public OrderStatus getStatus() {
        return status;
    }
}