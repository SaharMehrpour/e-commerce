package com.ecommerce.order.messaging;

import com.ecommerce.shared.domain.order.OrderStatus;
import com.ecommerce.shared.event.order.OrderCancelledEvent;
import com.ecommerce.shared.event.order.OrderCreatedEvent;
import com.ecommerce.shared.event.order.OrderUpdatedEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.*;

class OrderEventProducerTest {

	private KafkaTemplate<String, Object> kafkaTemplate;
	private OrderEventProducer producer;

	private static final String CREATED_TOPIC = "order-created";
	private static final String CANCELLED_TOPIC = "order-cancelled";
	private static final String UPDATED_TOPIC = "order-updated";

	@SuppressWarnings("unchecked")
	@BeforeEach
	void setUp() {
		kafkaTemplate = mock(KafkaTemplate.class);

		producer = new OrderEventProducer(
				kafkaTemplate,
				CREATED_TOPIC,
				CANCELLED_TOPIC,
				UPDATED_TOPIC);
	}

	@Test
	void shouldSendOrderCreatedEvent() {

		OrderCreatedEvent event = new OrderCreatedEvent("order-1", "u1", "p1", 2, OrderStatus.CREATED);

		producer.sendOrderCreatedEvent(event);

		verify(kafkaTemplate).send(
				CREATED_TOPIC,
				"order-1",
				event);
	}

	@Test
	void shouldSendOrderCancelledEvent() {

		OrderCancelledEvent event = new OrderCancelledEvent("order-1", "u1", "p1", 2, OrderStatus.CANCELLED);

		producer.sendOrderCancelledEvent(event);

		verify(kafkaTemplate).send(
				CANCELLED_TOPIC,
				"order-1",
				event);
	}

	@Test
	void shouldSendOrderUpdatedEvent() {

		OrderUpdatedEvent event = new OrderUpdatedEvent("order-1", "p1", 2, "updated-product", 3);

		producer.sendOrderUpdatedEvent(event);

		verify(kafkaTemplate).send(
				UPDATED_TOPIC,
				"order-1",
				event);
	}
}