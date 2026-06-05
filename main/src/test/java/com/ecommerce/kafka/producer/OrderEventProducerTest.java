package com.ecommerce.kafka.producer;

import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.messaging.OrderEventProducer;
import com.ecommerce.shared.event.OrderCancelledEvent;
import com.ecommerce.shared.event.OrderCreatedEvent;
import com.ecommerce.shared.event.OrderUpdatedEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

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
				"order-updated");
	}

	@Test
	void shouldSendOrderCreatedEvent() {

		OrderCreatedEvent event = new OrderCreatedEvent("order-1", "u1", "p1", 2, OrderStatus.CREATED);
		ReflectionTestUtils.setField(event, "eventId", "event-1");

		producer.sendOrderCreatedEvent(event);

		verify(kafkaTemplate).send(
				"order-created",
				"order-1",
				event);
	}

	@Test
	void shouldSendOrderCancelledEvent() {

		OrderCancelledEvent event = new OrderCancelledEvent("order-1", "u1", "p1", 2, OrderStatus.CANCELLED);
		ReflectionTestUtils.setField(event, "eventId", "event-2");

		producer.sendOrderCancelledEvent(event);

		verify(kafkaTemplate).send(
				"order-cancelled",
				"order-1",
				event);
	}

	@Test
	void shouldSendOrderUpdatedEvent() {
		OrderUpdatedEvent event = new OrderUpdatedEvent("order-1", "p1", 2, "updated-product", 3);
		ReflectionTestUtils.setField(event, "eventId", "event-3");

		producer.sendOrderUpdatedEvent(event);

		verify(kafkaTemplate).send(
				"order-updated",
				"order-1",
				event);
	}
}