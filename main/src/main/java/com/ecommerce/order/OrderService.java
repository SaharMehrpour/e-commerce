package com.ecommerce.order;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.InventoryRequest;
import com.ecommerce.dto.InventoryResponse;
import com.ecommerce.dto.UpdateOrderRequest;
import com.ecommerce.event.OrderCancelledEvent;
import com.ecommerce.event.OrderCreatedEvent;
import com.ecommerce.event.OrderUpdatedEvent;
import com.ecommerce.exception.InvalidOrderException;
import com.ecommerce.exception.InventoryNotEnoughException;
import com.ecommerce.exception.InventoryNotFoundException;
import com.ecommerce.exception.OrderAlreadyCancelledException;
import com.ecommerce.exception.OrderNotFoundException;
import com.ecommerce.exception.OrderNotUpdatableException;
import com.ecommerce.inventory.InventoryService;
import com.ecommerce.kafka.producer.OrderKafkaProducer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository repository;
    private final OrderKafkaProducer kafkaProducer;
    private final InventoryService inventoryService;
    private final RestTemplate restTemplate;

    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;

    public OrderService(OrderRepository repository, OrderKafkaProducer kafkaProducer,
        InventoryService inventoryService, RestTemplate restTemplate) {
        this.repository = repository;
        this.kafkaProducer = kafkaProducer;
        this.inventoryService = inventoryService;
        this.restTemplate = restTemplate;
    }

    @Caching(
            put = @CachePut(value = "orders", key = "#result.id", unless = "#result.id == null"),
            evict = @CacheEvict(value = "orders", key = "'all'")
    )
    @Transactional
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

        InventoryRequest inventoryRequest = new InventoryRequest();
        inventoryRequest.setProductId(order.getProductId());
        inventoryRequest.setQuantity(order.getQuantity());
        inventoryService.releaseStock(inventoryRequest);

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
    @Transactional
    public Optional<Order> updateOrder(String id, UpdateOrderRequest request) {

        Order order = repository.findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException("Order not found with id: " + id)
                );
        
        if ("CANCELLED".equals(order.getStatus())) {
            throw new OrderNotUpdatableException("Cannot update a cancelled order");
        }
        
        String oldProductId = order.getProductId();
        String newProductId = request.getProductId();
        Integer oldQuantity = order.getQuantity();
        Integer newQuantity = request.getQuantity();

        if (newQuantity == null && newProductId == null) {
            throw new InvalidOrderException("At least one field (productId or quantity) must be provided for update");
        }

        if (newQuantity != null && newQuantity <= 0) {
            throw new InvalidOrderException("Quantity must be greater than zero");
        }

        // release stock for old product if productId is being updated
        if (newProductId != null) {
            InventoryRequest releaseRequest = new InventoryRequest();
            releaseRequest.setProductId(oldProductId);
            releaseRequest.setQuantity(oldQuantity);
            inventoryService.releaseStock(releaseRequest);
            
            order.setProductId(newProductId);
        }

        if (newProductId == null) {
            newProductId = oldProductId;
        }
        if (newQuantity == null) {
            newQuantity = oldQuantity;
        }
        InventoryRequest reserveRequest = new InventoryRequest();
        reserveRequest.setProductId(newProductId);
        reserveRequest.setQuantity(newQuantity);
        inventoryService.reserveStock(reserveRequest);

        order.setQuantity(newQuantity);

        Order updatedOrder = repository.save(order);

        OrderUpdatedEvent event = new OrderUpdatedEvent(
                UUID.randomUUID().toString(),
                updatedOrder.getId(),
                updatedOrder.getProductId(),
                updatedOrder.getQuantity());

        kafkaProducer.sendOrderUpdatedEvent(event);

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
