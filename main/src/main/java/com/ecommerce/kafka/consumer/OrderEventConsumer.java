package com.ecommerce.kafka.consumer;

import com.ecommerce.dto.InventoryRequest;
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
    public void handleOrderCreated(OrderCreatedEvent event) {
        System.out.println("📦 Order CREATED received: (Event) " + event);

        InventoryRequest request = new InventoryRequest();
        request.setProductId(event.getProductId());
        request.setQuantity(event.getQuantity());

        inventoryService.reserveStock(request);
    }

    @KafkaListener(
        topics = "${app.kafka.topics.order-cancelled}",
        containerFactory = "orderCancelledKafkaListenerFactory"
    )
    public void handleOrderCancelled(OrderCancelledEvent event) {
        System.out.println("❌ Order CANCELLED received: " + event);

        InventoryRequest request = new InventoryRequest();
        request.setProductId(event.getProductId());
        request.setQuantity(event.getQuantity());

        inventoryService.releaseStock(request);
    }

    @KafkaListener(
        topics = "${app.kafka.topics.order-updated}",
        containerFactory = "orderUpdatedKafkaListenerFactory"
    )
    public void handleOrderUpdated(OrderUpdatedEvent event) {
        System.out.println("🔄 Order UPDATED received: " + event);

        if (!event.getOldProductId()
                .equals(event.getNewProductId())) {

            InventoryRequest releaseRequest = new InventoryRequest();
            releaseRequest.setProductId(event.getOldProductId());
            releaseRequest.setQuantity(event.getOldQuantity());
            inventoryService.releaseStock(releaseRequest);

            InventoryRequest reserveRequest = new InventoryRequest();
            reserveRequest.setProductId(event.getNewProductId());
            reserveRequest.setQuantity(event.getNewQuantity());          
            inventoryService.reserveStock(reserveRequest);
            return;
        }

        int quantityDelta = event.getNewQuantity() - event.getOldQuantity();

        if (quantityDelta == 0) {
            return;
        }

        InventoryRequest request = new InventoryRequest();
        request.setProductId(event.getNewProductId());
        request.setQuantity(Math.abs(quantityDelta));

        if (quantityDelta > 0) {
            inventoryService.reserveStock(request);
            return;
        }

        inventoryService.releaseStock(request);
    }
}
