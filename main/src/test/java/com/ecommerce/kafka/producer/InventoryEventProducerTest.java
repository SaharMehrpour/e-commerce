package com.ecommerce.kafka.producer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import com.ecommerce.event.InventoryFailedEvent;
import com.ecommerce.event.InventoryReservedEvent;
import com.ecommerce.event.InventoryRestoredEvent;
import com.ecommerce.event.InventoryUpdatedEvent;

public class InventoryEventProducerTest {

    private KafkaTemplate<String, com.ecommerce.event.Event> kafkaTemplate;
    private InventoryEventProducer producer;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);

        producer = new InventoryEventProducer(
                kafkaTemplate,
                "inventory-reserved",
                "inventory-restored",
                "inventory-failed",
                "inventory-updated"
        );
    }

    @Test
    void shouldSendInventoryReservedEvent() {
        InventoryReservedEvent event = new InventoryReservedEvent(
                "event-1",
                "p1",
                2
            );

        producer.sendInventoryReservedEvent(event);

        verify(kafkaTemplate).send(
                "inventory-reserved",
                "event-1",
                event
        );
    }

    @Test
    void shouldSendInventoryRestoredEvent() {
        InventoryRestoredEvent event = new InventoryRestoredEvent(
                "event-2",
                "p1",
                2
            );

        producer.sendInventoryRestoredEvent(event);

        verify(kafkaTemplate).send(
                "inventory-restored",
                "event-2",
                event
        );
    }

    @Test
    void shouldSendInventoryFailedEvent() {
        InventoryFailedEvent event = new InventoryFailedEvent(
                "event-3",
                "FAILED_ACTION",
                "FAILURE_REASON"
        );

        producer.sendInventoryFailedEvent(event);

        verify(kafkaTemplate).send(
                "inventory-failed",
                "event-3",
                event
        );
    }

    @Test
    void shouldSendInventoryUpdatedEvent() {
        InventoryUpdatedEvent event = new InventoryUpdatedEvent(
                "event-4",
                "p1",
                2,
                3
        );

        producer.sendInventoryUpdatedEvent(event);

        verify(kafkaTemplate).send(
                "inventory-updated",
                "event-4",
                event
        );
    }
}
