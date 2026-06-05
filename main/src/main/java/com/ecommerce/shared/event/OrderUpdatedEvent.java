package com.ecommerce.shared.event;

import java.time.Instant;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class OrderUpdatedEvent extends Event {
    
    private String orderId;
    private String oldProductId;
    private Integer oldQuantity;
    private String newProductId;
    private Integer newQuantity;

    public OrderUpdatedEvent(
        String eventId,
        String orderId,
        String oldProductId,
        Integer oldQuantity,
        String newProductId,
        Integer newQuantity
    ) {
        super(
                eventId,
                "ORDER_UPDATED",
                Instant.now().toString()
        );

        this.orderId = orderId;
        this.oldProductId = oldProductId;
        this.oldQuantity = oldQuantity;
        this.newProductId = newProductId;
        this.newQuantity = newQuantity;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getOldProductId() {
        return oldProductId;
    }

    public Integer getOldQuantity() {
        return oldQuantity;
    }

    public String getNewProductId() {
        return newProductId;
    }

    public Integer getNewQuantity() {
        return newQuantity;
    }
}