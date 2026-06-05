package com.ecommerce.shared.event;

import com.ecommerce.order.domain.OrderStatus;

public class OrderCancelledEvent extends Event {

    private String orderId;
    private String userId;
    private String productId;
    private Integer quantity;
    private OrderStatus status;

    public OrderCancelledEvent(
            String orderId,
            String userId,
            String productId,
            Integer quantity,
            OrderStatus status) {
        super(EventType.ORDER_CANCELLED);

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

    public Integer getQuantity() {
        return quantity;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
}