package com.ecommerce.kafka.producer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.ecommerce.event.InventoryFailedEvent;
import com.ecommerce.event.InventoryReservedEvent;
import com.ecommerce.event.InventoryRestoredEvent;
import com.ecommerce.event.InventoryUpdatedEvent;

@Service
public class InventoryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String inventoryReservedTopic;
    private final String inventoryRestoredTopic;
    private final String inventoryFailedTopic;
    private final String inventoryUpdatedTopic;

    public InventoryEventProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topics.inventory-reserved}") String inventoryReservedTopic,
            @Value("${app.kafka.topics.inventory-restored}") String inventoryRestoredTopic,
            @Value("${app.kafka.topics.inventory-failed}") String inventoryFailedTopic,
            @Value("${app.kafka.topics.inventory-updated}") String inventoryUpdatedTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.inventoryReservedTopic = inventoryReservedTopic;
        this.inventoryRestoredTopic = inventoryRestoredTopic;
        this.inventoryFailedTopic = inventoryFailedTopic;
        this.inventoryUpdatedTopic = inventoryUpdatedTopic;
    }

    public void sendInventoryReservedEvent(InventoryReservedEvent event) {
        kafkaTemplate.send(inventoryReservedTopic, event.getEventId(), event);
    }

    public void sendInventoryRestoredEvent(InventoryRestoredEvent event) {
        kafkaTemplate.send(inventoryRestoredTopic, event.getEventId(), event);
    }

    public void sendInventoryFailedEvent(InventoryFailedEvent event) {
        kafkaTemplate.send(inventoryFailedTopic, event.getEventId(), event);
    }

    public void sendInventoryUpdatedEvent(InventoryUpdatedEvent event) {
        kafkaTemplate.send(inventoryUpdatedTopic, event.getEventId(), event);
    }
}
