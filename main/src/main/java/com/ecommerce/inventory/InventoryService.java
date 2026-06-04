package com.ecommerce.inventory;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.dto.InventoryRequest;
import com.ecommerce.event.InventoryFailedEvent;
import com.ecommerce.event.InventoryReservedEvent;
import com.ecommerce.event.InventoryRestoredEvent;
import com.ecommerce.event.InventoryUpdatedEvent;
import com.ecommerce.exception.InvalidInventoryException;
import com.ecommerce.exception.InventoryNotEnoughException;
import com.ecommerce.exception.InventoryNotFoundException;
import com.ecommerce.kafka.producer.InventoryEventProducer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryEventProducer kafkaProducer;

    public InventoryService(InventoryRepository inventoryRepository,
        InventoryEventProducer kafkaProducer) {
        this.inventoryRepository = inventoryRepository;
        this.kafkaProducer = kafkaProducer;
    }

    @Caching(
        put = @CachePut(value = "inventory", key = "#request.productId"),
        evict = @CacheEvict(value = "inventoryList", allEntries = true)
    )
    @Transactional
    public Optional<InventoryItem> addStock(InventoryRequest request) {
        validateInventoryRequest(request);

        String productId = request.getProductId();
        Integer quantity = request.getQuantity();

        InventoryItem item = inventoryRepository.findByProductId(productId)
                .orElseGet(() -> new InventoryItem(productId, 0, 0));

        item.setAvailableQuantity(item.getAvailableQuantity() + quantity);
        item = inventoryRepository.save(item);

        kafkaProducer.sendInventoryUpdatedEvent(new InventoryUpdatedEvent(
                UUID.randomUUID().toString(),
                productId,
                item.getAvailableQuantity(),
                item.getReservedQuantity()));

        return Optional.of(item);
    }

    @Caching(
        put = @CachePut(value = "inventory", key = "#request.productId"),
        evict = @CacheEvict(value = "inventoryList", allEntries = true)
    )
    @Transactional
    public Optional<InventoryItem> reserveStock(InventoryRequest request) {
        validateInventoryRequest(request);

        String productId = request.getProductId();
        Integer quantity = request.getQuantity();

        InventoryItem item = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    InventoryFailedEvent event = new InventoryFailedEvent(
                            UUID.randomUUID().toString(),
                            "RESERVE_STOCK_FAILED",
                            "PRODUCT_NOT_FOUND");
                    event.setProductId(productId);
                    event.setQuantity(quantity);
                    kafkaProducer.sendInventoryFailedEvent(event);

                    return new InventoryNotFoundException("Inventory item not found for product " + productId);
                });

        if (item.getAvailableQuantity() < quantity) {
            InventoryFailedEvent event = new InventoryFailedEvent(
                    UUID.randomUUID().toString(),
                    "RESERVE_STOCK_FAILED",
                    "INSUFFICIENT_STOCK");
            event.setProductId(productId);
            event.setQuantity(quantity);
            kafkaProducer.sendInventoryFailedEvent(event);

            throw new InventoryNotEnoughException("Insufficient stock for product " + productId);
        }

        item.setAvailableQuantity(item.getAvailableQuantity() - quantity);
        item.setReservedQuantity(item.getReservedQuantity() + quantity);

        inventoryRepository.save(item);

        kafkaProducer.sendInventoryReservedEvent(new InventoryReservedEvent(
                UUID.randomUUID().toString(),
                productId,
                quantity));

        return Optional.of(item);
    }

    @Caching(
        put = @CachePut(value = "inventory", key = "#request.productId"),
        evict = @CacheEvict(value = "inventoryList", allEntries = true)
    )
    @Transactional
    public Optional<InventoryItem> deductStock(InventoryRequest request) {
        validateInventoryRequest(request);

        String productId = request.getProductId();
        Integer quantity = request.getQuantity();

        InventoryItem item = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    InventoryFailedEvent event = new InventoryFailedEvent(
                            UUID.randomUUID().toString(),
                            "DEDUCT_STOCK_FAILED",
                            "PRODUCT_NOT_FOUND");
                    event.setProductId(productId);
                    event.setQuantity(quantity);
                    kafkaProducer.sendInventoryFailedEvent(event);

                    return new InventoryNotFoundException("Inventory item not found for product " + productId);
                });

        if (item.getReservedQuantity() < quantity) {
            throw new InventoryNotEnoughException(
                    "Reserved stock is less than requested quantity for product " + productId
            );
        }

        item.setReservedQuantity(item.getReservedQuantity() - quantity);
        item = inventoryRepository.save(item);

        kafkaProducer.sendInventoryUpdatedEvent(new InventoryUpdatedEvent(
                UUID.randomUUID().toString(),
                productId,
                item.getAvailableQuantity(),
                item.getReservedQuantity()));

        return Optional.of(item);
    }

    @Caching(
        put = @CachePut(value = "inventory", key = "#request.productId"),
        evict = @CacheEvict(value = "inventoryList", allEntries = true)
    )
    @Transactional
    public Optional<InventoryItem> releaseStock(InventoryRequest request) {
        validateInventoryRequest(request);

        String productId = request.getProductId();
        Integer quantity = request.getQuantity();

        InventoryItem item = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    InventoryFailedEvent event = new InventoryFailedEvent(
                            UUID.randomUUID().toString(),
                            "RELEASE_STOCK_FAILED",
                            "PRODUCT_NOT_FOUND");
                    event.setProductId(productId);
                    event.setQuantity(quantity);
                    kafkaProducer.sendInventoryFailedEvent(event);

                    return new InventoryNotFoundException("Inventory item not found for product " + productId);
                });

        if (item.getReservedQuantity() < quantity) {
            throw new InventoryNotEnoughException(
                    "Reserved stock is less than requested quantity for product " + productId
            );
        }

        item.setAvailableQuantity(item.getAvailableQuantity() + quantity);
        item.setReservedQuantity(item.getReservedQuantity() - quantity);

        inventoryRepository.save(item);

        kafkaProducer.sendInventoryRestoredEvent(new InventoryRestoredEvent(
                UUID.randomUUID().toString(),
                productId,
                quantity));

        return Optional.of(item);
    }

    @Cacheable(value = "inventory", key = "#productId")
    public Optional<InventoryItem> getInventory(String productId) {
        return inventoryRepository.findByProductId(productId);
    }

    @Cacheable(value = "inventoryList")
    public List<InventoryItem> getAllInventory() {
        return inventoryRepository.findAll();
    }

    private void validateInventoryRequest(InventoryRequest request) {
        if (request == null) {
            throw new InvalidInventoryException("Inventory request is required");
        }

        if (request.getProductId() == null || request.getProductId().isBlank()) {
            throw new InvalidInventoryException("Product ID is required");
        }

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new InvalidInventoryException("Quantity must be greater than zero");
        }
    }
}
