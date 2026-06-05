package com.ecommerce.kafka.producer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.ecommerce.inventory.messaging.InventoryEventProducer;
import com.ecommerce.shared.event.InventoryFailedEvent;
import com.ecommerce.shared.event.InventoryReservedEvent;
import com.ecommerce.shared.event.InventoryRestoredEvent;
import com.ecommerce.shared.event.InventoryUpdatedEvent;

public class InventoryEventProducerTest {

    private KafkaTemplate<String, Object> kafkaTemplate;
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
        InventoryReservedEvent event = new InventoryReservedEvent("p1", 2);
        ReflectionTestUtils.setField(event, "eventId", "event-1");

        producer.sendInventoryReservedEvent(event);

        verify(kafkaTemplate).send(
                "inventory-reserved",
                "event-1",
                event
        );
    }

    @Test
    void shouldSendInventoryRestoredEvent() {
        InventoryRestoredEvent event = new InventoryRestoredEvent("p1", 2);
        ReflectionTestUtils.setField(event, "eventId", "event-2");

        producer.sendInventoryRestoredEvent(event);

        verify(kafkaTemplate).send(
                "inventory-restored",
                "event-2",
                event
        );
    }

    @Test
    void shouldSendInventoryFailedEvent() {
        InventoryFailedEvent event = new InventoryFailedEvent("FAILED_ACTION", "FAILURE_REASON");
        ReflectionTestUtils.setField(event, "eventId", "event-3");

        producer.sendInventoryFailedEvent(event);

        verify(kafkaTemplate).send(
                "inventory-failed",
                "event-3",
                event
        );
    }

    @Test
    void shouldSendInventoryUpdatedEvent() {
        InventoryUpdatedEvent event = new InventoryUpdatedEvent("p1", 2, 3);
        ReflectionTestUtils.setField(event, "eventId", "event-4");

        producer.sendInventoryUpdatedEvent(event);

        verify(kafkaTemplate).send(
                "inventory-updated",
                "event-4",
                event
        );
    }
}
