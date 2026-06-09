package com.ecommerce.inventory.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.ecommerce.shared.event.InventoryFailedEvent;
import com.ecommerce.shared.event.InventoryReservedEvent;
import com.ecommerce.shared.event.InventoryRestoredEvent;
import com.ecommerce.shared.event.InventoryUpdatedEvent;

@Service
public class InventoryEventConsumer {
    
    @KafkaListener(
        topics = "${app.kafka.topics.inventory-reserved}",
        containerFactory = "inventoryReservedEventKafkaListenerFactory"
    )
    public void handleInventoryReservedEvent(InventoryReservedEvent event) {
        System.out.println("✅ Inventory RESERVED received: " + event);   
    }

    @KafkaListener(
        topics = "${app.kafka.topics.inventory-restored}",
        containerFactory = "inventoryRestoredEventKafkaListenerFactory"
    )
    public void handleInventoryRestoredEvent(InventoryRestoredEvent event) {
        System.out.println("♻️ Inventory RESTORED received: " + event);
    }

    @KafkaListener(
        topics = "${app.kafka.topics.inventory-failed}",
        containerFactory = "inventoryFailedEventKafkaListenerFactory"
    )
    public void handleInventoryFailedEvent(InventoryFailedEvent event) {
        System.out.println("❌ Inventory FAILED received: " + event);
    }

    @KafkaListener(
        topics = "${app.kafka.topics.inventory-updated}",
        containerFactory = "inventoryUpdatedEventKafkaListenerFactory"
    )
    public void handleInventoryUpdatedEvent(InventoryUpdatedEvent event) {
        System.out.println("🔄 Inventory UPDATED received: " + event);
    }
}
