package com.ecommerce.order;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository repository;
    private final OrderKafkaProducer kafkaProducer;

    public OrderService(OrderRepository repository, OrderKafkaProducer kafkaProducer) {
        this.repository = repository;
        this.kafkaProducer = kafkaProducer;
    }

    public Order createOrder(Order order) {

        order.setStatus("CREATED");
        Order savedOrder = repository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getUserId(),
                savedOrder.getProductId(),
                savedOrder.getQuantity(),
                savedOrder.getStatus()
        );

        kafkaProducer.sendOrderCreatedEvent(event);

        return savedOrder;
    }

    public List<Order> getOrders() {
        return repository.findAll();
    }

    public Optional<Order> getOrder(String id) {
        return repository.findById(id);
    }

}
