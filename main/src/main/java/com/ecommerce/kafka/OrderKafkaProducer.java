package com.ecommerce.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.ecommerce.event.Event;
import com.ecommerce.event.OrderCreatedEvent;

@Service
public class OrderKafkaProducer {

    private final KafkaTemplate<String, Event> kafkaTemplate;
    private final String orderCreatedTopic;

    public OrderKafkaProducer(
            KafkaTemplate<String, Event> kafkaTemplate,
            @Value("${app.kafka.topics.order-created}") String orderCreatedTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderCreatedTopic = orderCreatedTopic;
    }

    public void sendOrderCreatedEvent(OrderCreatedEvent event) {
        kafkaTemplate.send(orderCreatedTopic, event.getOrderId(), event);
    }
}
