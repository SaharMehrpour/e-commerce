package com.ecommerce.kafka.producer;

import com.ecommerce.order.messaging.OrderEventProducer;
import com.ecommerce.shared.event.OrderCancelledEvent;
import com.ecommerce.shared.event.OrderCreatedEvent;
import com.ecommerce.shared.event.OrderUpdatedEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.*;

class OrderEventProducerTest {

    private KafkaTemplate<String, Object> kafkaTemplate;
    private OrderEventProducer producer;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);

        producer = new OrderEventProducer(
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