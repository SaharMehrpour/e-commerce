package com.ecommerce.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.ecommerce.event.Event;

@Service
public class InventoryEventConsumer {
    
    @KafkaListener(
        topics = "${app.kafka.topics.inventory-reserved}",
        containerFactory = "inventoryReservedEventKafkaListenerFactory"
    )
    public void handleInventoryReservedEvent(Event event) {
        System.out.println("✅ Inventory RESERVED received: " + event);   
    }

    @KafkaListener(
        topics = "${app.kafka.topics.inventory-restored}",
        containerFactory = "inventoryRestoredEventKafkaListenerFactory"
    )
    public void handleInventoryRestoredEvent(Event event) {
        System.out.println("♻️ Inventory RESTORED received: " + event);
    }

    @KafkaListener(
        topics = "${app.kafka.topics.inventory-failed}",
        containerFactory = "inventoryFailedEventKafkaListenerFactory"
    )
    public void handleInventoryFailedEvent(Event event) {
        System.out.println("❌ Inventory FAILED received: " + event);
    }

    @KafkaListener(
        topics = "${app.kafka.topics.inventory-updated}",
        containerFactory = "inventoryUpdatedEventKafkaListenerFactory"
    )
    public void handleInventoryUpdatedEvent(Event event) {
        System.out.println("🔄 Inventory UPDATED received: " + event);
    }
}
