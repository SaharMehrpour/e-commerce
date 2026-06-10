package com.ecommerce.shared.event.order;

import com.ecommerce.shared.domain.order.OrderStatus;
import com.ecommerce.shared.event.Event;
import com.ecommerce.shared.event.EventType;

public class OrderCancelledEvent extends Event {

    private String orderId;
    private String userId;
    private String productId;
    private Integer quantity;
    private OrderStatus status;

    public OrderCancelledEvent() {
        super(EventType.ORDER_CANCELLED);
    }

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