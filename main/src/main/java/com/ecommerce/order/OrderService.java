package com.ecommerce.order;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.UpdateOrderRequest;
import com.ecommerce.event.OrderCancelledEvent;
import com.ecommerce.event.OrderCreatedEvent;
import com.ecommerce.event.OrderUpdatedEvent;
import com.ecommerce.exception.InvalidOrderException;
import com.ecommerce.exception.OrderAlreadyCancelledException;
import com.ecommerce.exception.OrderNotFoundException;
import com.ecommerce.exception.OrderNotUpdatableException;
import com.ecommerce.kafka.producer.OrderKafkaProducer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public Order createOrder(CreateOrderRequest request) {
        if (request.getUserId() == null || request.getUserId().isBlank()) {
            throw new InvalidOrderException("User ID is required");
        }

        if (request.getProductId() == null || request.getProductId().isBlank()) {
            throw new InvalidOrderException("Product ID is required");
        }

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new InvalidOrderException("Quantity must be greater than zero");
        }

        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setProductId(request.getProductId());
        order.setQuantity(request.getQuantity());
        order.setStatus("CREATED");
        Order savedOrder = repository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID().toString(),
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

        Order order = repository.findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException("Order not found with id: " + id)
                );

        if ("CANCELLED".equals(order.getStatus())) {
            throw new OrderAlreadyCancelledException("Order is already cancelled");
        }

        order.setStatus("CANCELLED");

        Order updatedOrder = repository.save(order);

        OrderCancelledEvent event =
                new OrderCancelledEvent(
                        UUID.randomUUID().toString(),
                        updatedOrder.getId(),
                        updatedOrder.getUserId(),
                        updatedOrder.getStatus()
                );

        kafkaProducer.sendOrderCancelledEvent(event);

        return Optional.of(updatedOrder);
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
    public Optional<Order> updateOrder(String id, UpdateOrderRequest request) {

        Optional<Order> optionalOrder = repository.findById(id);

        optionalOrder.ifPresent(order -> {

            if ("CANCELLED".equals(order.getStatus())) {
                throw new OrderNotUpdatableException("Cannot update a cancelled order");
            }

            if (request.getQuantity() != null) {

                if (request.getQuantity() <= 0) {
                    throw new InvalidOrderException("Quantity must be greater than zero");
                }

                order.setQuantity(request.getQuantity());
            }

            if (request.getProductId() != null) {
                order.setProductId(request.getProductId());
            }

            repository.save(order);
            
            OrderUpdatedEvent event = new OrderUpdatedEvent(
                    UUID.randomUUID().toString(),
                    order.getId(),
                    order.getProductId(),
                    order.getQuantity()
            );
            kafkaProducer.sendOrderUpdatedEvent(event);
        });

        return optionalOrder;
    }
}
