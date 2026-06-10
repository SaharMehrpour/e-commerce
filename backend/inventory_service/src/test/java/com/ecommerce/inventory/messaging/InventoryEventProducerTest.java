package com.ecommerce.inventory.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import com.ecommerce.shared.event.inventory.InventoryFailedEvent;
import com.ecommerce.shared.event.inventory.InventoryReservedEvent;
import com.ecommerce.shared.event.inventory.InventoryRestoredEvent;
import com.ecommerce.shared.event.inventory.InventoryUpdatedEvent;

import static org.mockito.Mockito.*;

class InventoryEventProducerTest {

    private KafkaTemplate<String, Object> kafkaTemplate;
    private InventoryEventProducer producer;

    private static final String RESERVED_TOPIC = "inventory-reserved";
    private static final String RESTORED_TOPIC = "inventory-restored";
    private static final String FAILED_TOPIC = "inventory-failed";
    private static final String UPDATED_TOPIC = "inventory-updated";

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);

        producer = new InventoryEventProducer(
                kafkaTemplate,
                RESERVED_TOPIC,
                RESTORED_TOPIC,
                FAILED_TOPIC,
                UPDATED_TOPIC);
    }

    @Test
    void shouldSendInventoryReservedEvent() {

        InventoryReservedEvent event = new InventoryReservedEvent("p1", 2);

        producer.sendInventoryReservedEvent(event);

        verify(kafkaTemplate).send(
                RESERVED_TOPIC,
                event.getEventId(),
                event);
    }

    @Test
    void shouldSendInventoryRestoredEvent() {

        InventoryRestoredEvent event = new InventoryRestoredEvent("p1", 2);

        producer.sendInventoryRestoredEvent(event);

        verify(kafkaTemplate).send(
                RESTORED_TOPIC,
                event.getEventId(),
                event);
    }

    @Test
    void shouldSendInventoryFailedEvent() {

        InventoryFailedEvent event = new InventoryFailedEvent("FAILED_ACTION", "FAILURE_REASON");

        producer.sendInventoryFailedEvent(event);

        verify(kafkaTemplate).send(
                FAILED_TOPIC,
                event.getEventId(),
                event);
    }

    @Test
    void shouldSendInventoryUpdatedEvent() {

        InventoryUpdatedEvent event = new InventoryUpdatedEvent("p1", 2, 3);

        producer.sendInventoryUpdatedEvent(event);

        verify(kafkaTemplate).send(
                UPDATED_TOPIC,
                event.getEventId(),
                event);
    }
}