package com.ecommerce.order;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import com.ecommerce.event.OrderCancelledEvent;
import com.ecommerce.event.OrderCreatedEvent;
import com.ecommerce.kafka.OrderKafkaProducer;

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

    @Caching(
            put = @CachePut(value = "orders", key = "#result.id", unless = "#result.id == null"),
            evict = @CacheEvict(value = "orders", key = "'all'")
    )
    public Order createOrder(Order order) {

        order.setStatus("CREATED");
        Order savedOrder = repository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(
                "event-ID",
                savedOrder.getId(),
                savedOrder.getUserId(),
                savedOrder.getProductId(),
                savedOrder.getQuantity(),
                savedOrder.getStatus()
        );

        kafkaProducer.sendOrderCreatedEvent(event);

        return savedOrder;
    }

    @Cacheable(value = "orders", key = "'all'")
    public List<Order> getOrders() {
        return repository.findAll();
    }

    @Cacheable(value = "orders", key = "#id")
    public Optional<Order> getOrder(String id) {
        return repository.findById(id);
    }

    @Caching(
        put = @CachePut(
                value = "orders",
                key = "#id",
                unless = "#result == null"
        ),
        evict = @CacheEvict(
                value = "orders",
                key = "'all'"
        )
    )
    public Optional<Order> cancelOrder(String id) {

        Optional<Order> optionalOrder = repository.findById(id);

        optionalOrder.ifPresent(order -> {

            order.setStatus("CANCELLED");
            Order updatedOrder = repository.save(order);

            OrderCancelledEvent event =
                    new OrderCancelledEvent(
                            "event-ID",
                            updatedOrder.getId(),
                            updatedOrder.getUserId(),
                            updatedOrder.getStatus()
                    );

            kafkaProducer.sendOrderCancelledEvent(event);
        });

        return optionalOrder;
    }

}
