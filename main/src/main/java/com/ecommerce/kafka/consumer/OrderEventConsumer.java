package com.ecommerce.kafka.consumer;

import com.ecommerce.event.Event;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderEventConsumer {

    @KafkaListener(
        topics = "${app.kafka.topics.order-created}",
        containerFactory = "orderCreatedKafkaListenerFactory"
    )
    public void handleOrderCreated(Event event) {
        System.out.println("📦 Order CREATED received: (Event) " + event);
        System.out.println("Type of event: " + event.getClass().getName());
    }

    @KafkaListener(
        topics = "${app.kafka.topics.order-cancelled}",
        containerFactory = "orderCancelledKafkaListenerFactory"
    )
    public void handleOrderCancelled(Event event) {
        System.out.println("❌ Order CANCELLED received: " + event);
        System.out.println("Type of event: " + event.getClass().getName());
    }

}