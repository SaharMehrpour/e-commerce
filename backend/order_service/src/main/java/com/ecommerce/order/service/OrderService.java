package com.ecommerce.order.service;

import com.ecommerce.order.client.InventoryClient;
import com.ecommerce.order.domain.Order;
import com.ecommerce.order.messaging.OrderEventProducer;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.shared.domain.order.OrderStatus;
import com.ecommerce.shared.dto.inventory.InventoryResponse;
import com.ecommerce.shared.dto.order.CreateOrderRequest;
import com.ecommerce.shared.dto.order.UpdateOrderRequest;
import com.ecommerce.shared.event.order.OrderCancelledEvent;
import com.ecommerce.shared.event.order.OrderCreatedEvent;
import com.ecommerce.shared.event.order.OrderUpdatedEvent;
import com.ecommerce.shared.exception.inventory.InvalidOrderException;
import com.ecommerce.shared.exception.inventory.InventoryNotEnoughException;
import com.ecommerce.shared.exception.inventory.InventoryNotFoundException;
import com.ecommerce.shared.exception.order.OrderAlreadyCancelledException;
import com.ecommerce.shared.exception.order.OrderNotFoundException;
import com.ecommerce.shared.exception.order.OrderNotUpdatableException;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository repository;
    private final OrderEventProducer kafkaProducer;
    private final InventoryClient inventoryClient;

    private final Counter ordersCreatedCounter;
    private final Counter ordersCancelledCounter;

    public OrderService(
            OrderRepository repository,
            OrderEventProducer kafkaProducer,
            InventoryClient inventoryClient,
            MeterRegistry meterRegistry) {
        this.repository = repository;
        this.kafkaProducer = kafkaProducer;
        this.inventoryClient = inventoryClient;

        this.ordersCreatedCounter = Counter.builder("orders.created")
                .description("Total number of orders created")
                .register(meterRegistry);

        this.ordersCancelledCounter = Counter.builder("orders.cancelled")
                .description("Total number of orders cancelled")
                .register(meterRegistry);
    }

    @Caching(put = @CachePut(value = "orders", key = "#result.id", unless = "#result.id == null"), evict = @CacheEvict(value = "orders", key = "'all'"))
    @Transactional
    public Order createOrder(CreateOrderRequest request) {

        if (request == null
                || request.getUserId() == null || request.getUserId().isBlank()
                || request.getProductId() == null || request.getProductId().isBlank()
                || request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new InvalidOrderException("Invalid order request");
        }

        InventoryResponse inventoryResponse = Optional.ofNullable(inventoryClient.getInventory(request.getProductId()))
                .orElseThrow(() -> new InventoryNotFoundException(
                        "Inventory item not found for product " + request.getProductId()));

        if (inventoryResponse.getAvailableQuantity() < request.getQuantity()) {
            throw new InventoryNotEnoughException("Insufficient stock");
        }

        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setProductId(request.getProductId());
        order.setQuantity(request.getQuantity());
        order.setStatus(OrderStatus.CREATED);

        Order savedOrder = repository.save(order);

        ordersCreatedCounter.increment();

        kafkaProducer.sendOrderCreatedEvent(
                new OrderCreatedEvent(
                        savedOrder.getId(),
                        savedOrder.getUserId(),
                        savedOrder.getProductId(),
                        savedOrder.getQuantity(),
                        savedOrder.getStatus()));

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

    @Caching(put = @CachePut(value = "orders", key = "#id", unless = "#result == null"), evict = @CacheEvict(value = "orders", key = "'all'"))
    @Transactional
    public Optional<Order> cancelOrder(String id) {

        Order order = repository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (OrderStatus.CANCELLED.equals(order.getStatus())) {
            throw new OrderAlreadyCancelledException("Already cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);

        Order updated = repository.save(order);

        ordersCancelledCounter.increment();

        kafkaProducer.sendOrderCancelledEvent(
                new OrderCancelledEvent(
                        updated.getId(),
                        updated.getUserId(),
                        updated.getProductId(),
                        updated.getQuantity(),
                        updated.getStatus()));

        return Optional.of(updated);
    }

    @Caching(put = @CachePut(value = "orders", key = "#id", unless = "#result == null"), evict = @CacheEvict(value = "orders", key = "'all'"))
    @Transactional
    public Optional<Order> updateOrder(String id, UpdateOrderRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        
        Order order = repository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (OrderStatus.CANCELLED.equals(order.getStatus())) {
            throw new OrderNotUpdatableException("Cannot update cancelled order");
        }

        String oldProductId = order.getProductId();
        Integer oldQty = order.getQuantity();

        if (request.getProductId() != null) {
            order.setProductId(request.getProductId());
        }
        if (request.getQuantity() != null) {
            order.setQuantity(request.getQuantity());
        }

        Order updated = repository.save(order);

        kafkaProducer.sendOrderUpdatedEvent(
                new OrderUpdatedEvent(
                        updated.getId(),
                        oldProductId,
                        oldQty,
                        updated.getProductId(),
                        updated.getQuantity()));

        return Optional.of(updated);
    }
}