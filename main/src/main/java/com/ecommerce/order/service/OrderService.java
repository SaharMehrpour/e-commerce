package com.ecommerce.order.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.ecommerce.inventory.dto.InventoryResponse;
import com.ecommerce.order.domain.Order;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.UpdateOrderRequest;
import com.ecommerce.order.messaging.OrderEventProducer;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.shared.event.OrderCancelledEvent;
import com.ecommerce.shared.event.OrderCreatedEvent;
import com.ecommerce.shared.event.OrderUpdatedEvent;
import com.ecommerce.shared.exception.InvalidOrderException;
import com.ecommerce.shared.exception.InventoryNotEnoughException;
import com.ecommerce.shared.exception.InventoryNotFoundException;
import com.ecommerce.shared.exception.OrderAlreadyCancelledException;
import com.ecommerce.shared.exception.OrderNotFoundException;
import com.ecommerce.shared.exception.OrderNotUpdatableException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository repository;
    private final OrderEventProducer kafkaProducer;
    private final RestTemplate restTemplate;

    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;

    public OrderService(OrderRepository repository, OrderEventProducer kafkaProducer,
        RestTemplate restTemplate) {
        this.repository = repository;
        this.kafkaProducer = kafkaProducer;
        this.restTemplate = restTemplate;
    }

    @Caching(
            put = @CachePut(value = "orders", key = "#result.id", unless = "#result.id == null"),
            evict = @CacheEvict(value = "orders", key = "'all'")
    )
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        if (request == null) {
            throw new InvalidOrderException("Order request is required");
        }

        if (request.getUserId() == null || request.getUserId().isBlank()) {
            throw new InvalidOrderException("User ID is required");
        }

        if (request.getProductId() == null || request.getProductId().isBlank()) {
            throw new InvalidOrderException("Product ID is required");
        }

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new InvalidOrderException("Quantity must be greater than zero");
        }

        InventoryResponse inventoryResponse = getInventory(request.getProductId()).orElseThrow(() ->
                new InventoryNotFoundException(
                        "Inventory item not found for product " + request.getProductId()
                )
        );

        if (inventoryResponse.getAvailableQuantity() < request.getQuantity()) {
            throw new InventoryNotEnoughException(
                    "Insufficient stock for product " + request.getProductId()
                );
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
    @Transactional
    public Optional<Order> cancelOrder(String id) {

        Order order = repository.findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException("Order not found with id: " + id)
                );

        if ("CANCELLED".equals(order.getStatus())) {
            throw new OrderAlreadyCancelledException("Order is already cancelled");
        }

        String productId = order.getProductId();
        Integer quantity = order.getQuantity();

        order.setStatus("CANCELLED");

        Order updatedOrder = repository.save(order);

        OrderCancelledEvent event =
                new OrderCancelledEvent(
                        UUID.randomUUID().toString(),
                        updatedOrder.getId(),
                        updatedOrder.getUserId(),
                        productId,
                        quantity,
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
    @Transactional
    public Optional<Order> updateOrder(String id, UpdateOrderRequest request) {
        if (request == null) {
            throw new InvalidOrderException("Order update request is required");
        }

        Order order = repository.findById(id)
            .orElseThrow(() ->
                    new OrderNotFoundException(
                            "Order not found with id: " + id
                    )
            );

        if ("CANCELLED".equals(order.getStatus())) {
            throw new OrderNotUpdatableException(
                    "Cannot update a cancelled order"
            );
        }

        String oldProductId = order.getProductId();
        Integer oldQuantity = order.getQuantity();

        if (request.getProductId() != null) {
            if (request.getProductId().isBlank()) {
                throw new InvalidOrderException("Product ID is required");
            }

            order.setProductId(request.getProductId());
        }

        if (request.getQuantity() != null) {
            if (request.getQuantity() <= 0) {
                throw new InvalidOrderException(
                        "Quantity must be greater than zero"
                );
            }

            order.setQuantity(request.getQuantity());
        }

        Order updatedOrder = repository.save(order);

        kafkaProducer.sendOrderUpdatedEvent(
                new OrderUpdatedEvent(
                        UUID.randomUUID().toString(),
                        updatedOrder.getId(),
                        oldProductId,
                        oldQuantity,
                        updatedOrder.getProductId(),
                        updatedOrder.getQuantity()
                )
        );

        return Optional.of(updatedOrder);
    }

    public Optional<InventoryResponse> getInventory(String productId) {
        try {
            InventoryResponse response = restTemplate.getForObject(
                    inventoryServiceUrl + "/" + productId,
                    InventoryResponse.class
            );
            return Optional.ofNullable(response);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new InventoryNotFoundException(
                    "Inventory item not found for product " + productId
            );
        }
    }
}
