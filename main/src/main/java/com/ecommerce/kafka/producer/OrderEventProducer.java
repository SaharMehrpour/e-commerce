package com.ecommerce.kafka.producer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.ecommerce.event.Event;
import com.ecommerce.event.OrderCancelledEvent;
import com.ecommerce.event.OrderCreatedEvent;
import com.ecommerce.event.OrderUpdatedEvent;

@Service
public class OrderEventProducer {

    private final KafkaTemplate<String, Event> kafkaTemplate;
    private final String orderCreatedTopic;
    private final String orderCancelledTopic;
    private final String orderUpdatedTopic;

    public OrderEventProducer(
            KafkaTemplate<String, Event> kafkaTemplate,
            @Value("${app.kafka.topics.order-created}") String orderCreatedTopic,
            @Value("${app.kafka.topics.order-cancelled}") String orderCancelledTopic,
            @Value("${app.kafka.topics.order-updated}") String orderUpdatedTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderCreatedTopic = orderCreatedTopic;
        this.orderCancelledTopic = orderCancelledTopic;
        this.orderUpdatedTopic = orderUpdatedTopic;
    }

    public void sendOrderCreatedEvent(OrderCreatedEvent event) {
        kafkaTemplate.send(orderCreatedTopic, event.getOrderId(), event);
    }

    public void sendOrderCancelledEvent(OrderCancelledEvent event) {
        kafkaTemplate.send(orderCancelledTopic, event.getOrderId(), event);
    }

    public void sendOrderUpdatedEvent(OrderUpdatedEvent event) {
        kafkaTemplate.send(orderUpdatedTopic, event.getOrderId(), event);
    }
}
