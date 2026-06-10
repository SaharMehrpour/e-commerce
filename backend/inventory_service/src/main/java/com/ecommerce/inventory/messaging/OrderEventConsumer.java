package com.ecommerce.inventory.messaging;

import com.ecommerce.inventory.idempotency.ProcessedEvent;
import com.ecommerce.inventory.idempotency.ProcessedEventRepository;
import com.ecommerce.inventory.service.InventoryService;
import com.ecommerce.shared.dto.inventory.InventoryRequest;
import com.ecommerce.shared.event.order.OrderCancelledEvent;
import com.ecommerce.shared.event.order.OrderCreatedEvent;
import com.ecommerce.shared.event.order.OrderUpdatedEvent;

import java.time.Instant;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderEventConsumer {

    private final InventoryService inventoryService;
    private final ProcessedEventRepository processedEventRepository;

    public OrderEventConsumer(InventoryService inventoryService,
            ProcessedEventRepository processedEventRepository) {
        this.inventoryService = inventoryService;
        this.processedEventRepository = processedEventRepository;
    }

    @KafkaListener(
        topics = "${app.kafka.topics.order-created}",
        containerFactory = "orderCreatedKafkaListenerFactory"
    )
    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {
        System.out.println("📦 Order CREATED received: (Event) " + event);

        if (processedEventRepository.existsById(event.getEventId())) {
            System.out.println("Event is already processed: " + event.getEventId());
            return;
        }

        InventoryRequest request = new InventoryRequest();
        request.setProductId(event.getProductId());
        request.setQuantity(event.getQuantity());

        inventoryService.reserveStock(request);

        processedEventRepository.save(
            new ProcessedEvent(
                event.getEventId(),
                Instant.now()
            )
        );
    }

    @KafkaListener(
        topics = "${app.kafka.topics.order-cancelled}",
        containerFactory = "orderCancelledKafkaListenerFactory"
    )
    @Transactional
    public void handleOrderCancelled(OrderCancelledEvent event) {
        System.out.println("❌ Order CANCELLED received: " + event);

        if (processedEventRepository.existsById(event.getEventId())) {
            System.out.println("Event is already processed: " + event.getEventId());
            return;
        }

        InventoryRequest request = new InventoryRequest();
        request.setProductId(event.getProductId());
        request.setQuantity(event.getQuantity());

        inventoryService.releaseStock(request);

        processedEventRepository.save(
            new ProcessedEvent(
                event.getEventId(),
                Instant.now()
            )
        );
    }

    @KafkaListener(
        topics = "${app.kafka.topics.order-updated}",
        containerFactory = "orderUpdatedKafkaListenerFactory"
    )
    @Transactional
    public void handleOrderUpdated(OrderUpdatedEvent event) {
        System.out.println("🔄 Order UPDATED received: " + event);

        if (processedEventRepository.existsById(event.getEventId())) {
            return;
        }

        if (!event.getOldProductId().equals(event.getNewProductId())) {

            InventoryRequest releaseRequest = new InventoryRequest();
            releaseRequest.setProductId(event.getOldProductId());
            releaseRequest.setQuantity(event.getOldQuantity());
            inventoryService.releaseStock(releaseRequest);

            InventoryRequest reserveRequest = new InventoryRequest();
            reserveRequest.setProductId(event.getNewProductId());
            reserveRequest.setQuantity(event.getNewQuantity());
            inventoryService.reserveStock(reserveRequest);

        } else {

            int quantityDelta = event.getNewQuantity() - event.getOldQuantity();

            if (quantityDelta > 0) {
                InventoryRequest request = new InventoryRequest();
                request.setProductId(event.getNewProductId());
                request.setQuantity(quantityDelta);
                inventoryService.reserveStock(request);

            } else if (quantityDelta < 0) {
                InventoryRequest request = new InventoryRequest();
                request.setProductId(event.getNewProductId());
                request.setQuantity(Math.abs(quantityDelta));
                inventoryService.releaseStock(request);
            }
        }

        processedEventRepository.save(
            new ProcessedEvent(event.getEventId(), Instant.now())
        );
    }
}
