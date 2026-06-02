package com.ecommerce.kafka.producer;

import com.ecommerce.event.OrderCancelledEvent;
import com.ecommerce.event.OrderCreatedEvent;
import com.ecommerce.event.OrderUpdatedEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.*;

class OrderKafkaProducerTest {

    private KafkaTemplate<String, com.ecommerce.event.Event> kafkaTemplate;
    private OrderKafkaProducer producer;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);

        producer = new OrderKafkaProducer(
                kafkaTemplate,
                "order-created",
                "order-cancelled",
                "order-updated"
        );
    }

    @Test
    void shouldSendOrderCreatedEvent() {

        OrderCreatedEvent event = new OrderCreatedEvent(
                "event-1",
                "order-1",
                "u1",
                "p1",
                2,
                "CREATED"
        );

        producer.sendOrderCreatedEvent(event);

        verify(kafkaTemplate).send(
                "order-created",
                "order-1",
                event
        );
    }

    @Test
    void shouldSendOrderCancelledEvent() {

        OrderCancelledEvent event = new OrderCancelledEvent(
                "event-2",
                "order-1",
                "u1",
                "p1",
                2,
                "CANCELLED"
        );

        producer.sendOrderCancelledEvent(event);

        verify(kafkaTemplate).send(
                "order-cancelled",
                "order-1",
                event
        );
    }

    @Test
    void shouldSendOrderUpdatedEvent() {
        OrderUpdatedEvent event = new OrderUpdatedEvent(
                "event-3",
                "order-1",
                "p1",
                2,
                "updated-product",
                3
        );

        producer.sendOrderUpdatedEvent(event);

        verify(kafkaTemplate).send(
                "order-updated",
                "order-1",
                event
        );
    }
}