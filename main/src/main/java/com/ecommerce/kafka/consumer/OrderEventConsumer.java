package com.ecommerce.kafka.consumer;

import com.ecommerce.dto.InventoryRequest;
import com.ecommerce.event.Event;
import com.ecommerce.event.OrderCreatedEvent;
import com.ecommerce.event.OrderCancelledEvent;
import com.ecommerce.event.OrderUpdatedEvent;
import com.ecommerce.inventory.InventoryService;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderEventConsumer {

    private final InventoryService inventoryService;

    public OrderEventConsumer(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(
        topics = "${app.kafka.topics.order-created}",
        containerFactory = "orderCreatedKafkaListenerFactory"
    )
    public void handleOrderCreated(Event event) {
        System.out.println("📦 Order CREATED received: (Event) " + event);

        InventoryRequest request = new InventoryRequest();
        request.setProductId(((OrderCreatedEvent) event).getProductId());
        request.setQuantity(((OrderCreatedEvent) event).getQuantity());

        inventoryService.reserveStock(request);
    }

    @KafkaListener(
        topics = "${app.kafka.topics.order-cancelled}",
        containerFactory = "orderCancelledKafkaListenerFactory"
    )
    public void handleOrderCancelled(Event event) {
        System.out.println("❌ Order CANCELLED received: " + event);

        InventoryRequest request = new InventoryRequest();
        request.setProductId(((OrderCancelledEvent) event).getProductId());
        request.setQuantity(((OrderCancelledEvent) event).getQuantity());

        inventoryService.releaseStock(request);
    }

    @KafkaListener(
        topics = "${app.kafka.topics.order-updated}",
        containerFactory = "orderUpdatedKafkaListenerFactory"
    )
    public void handleOrderUpdated(Event event) {
        System.out.println("🔄 Order UPDATED received: " + event);

        OrderUpdatedEvent orderUpdatedEvent = (OrderUpdatedEvent) event;

        if (!orderUpdatedEvent.getOldProductId()
                .equals(orderUpdatedEvent.getNewProductId())) {

            InventoryRequest releaseRequest = new InventoryRequest();
            releaseRequest.setProductId(orderUpdatedEvent.getOldProductId());
            releaseRequest.setQuantity(orderUpdatedEvent.getOldQuantity());
            inventoryService.releaseStock(releaseRequest);

            InventoryRequest reserveRequest = new InventoryRequest();
            reserveRequest.setProductId(orderUpdatedEvent.getNewProductId());
            reserveRequest.setQuantity(orderUpdatedEvent.getNewQuantity());          
            inventoryService.reserveStock(reserveRequest);
            return;
        }

        InventoryRequest request = new InventoryRequest();
        request.setProductId(orderUpdatedEvent.getNewProductId());
        request.setQuantity(orderUpdatedEvent.getNewQuantity());
        inventoryService.reserveStock(request);
    }
}