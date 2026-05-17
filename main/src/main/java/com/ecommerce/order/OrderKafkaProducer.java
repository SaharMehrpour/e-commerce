package com.ecommerce.order;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderKafkaProducer {

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private final String orderCreatedTopic;

    public OrderKafkaProducer(
            KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate,
            @Value("${app.kafka.topics.order-created}") String orderCreatedTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderCreatedTopic = orderCreatedTopic;
    }

    public void sendOrderCreatedEvent(OrderCreatedEvent event) {
        kafkaTemplate.send(orderCreatedTopic, event.getOrderId(), event);
    }
}
