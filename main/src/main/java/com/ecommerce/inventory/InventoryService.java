package com.ecommerce.inventory;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.dto.InventoryRequest;
import com.ecommerce.exception.InventoryNotEnoughException;
import com.ecommerce.exception.InventoryNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Caching(
        put = @CachePut(value = "inventory", key = "#request.productId"),
        evict = @CacheEvict(value = "inventoryList", allEntries = true)
    )
    @Transactional
    public Optional<InventoryItem> addStock(InventoryRequest request) {
        String productId = request.getProductId();
        Integer quantity = request.getQuantity();

        InventoryItem item = inventoryRepository.findByProductId(productId)
                .orElseGet(() -> new InventoryItem(productId, 0, 0));

        item.setAvailableQuantity(item.getAvailableQuantity() + quantity);
        inventoryRepository.save(item);
        return Optional.of(item);
    }

    @Caching(
        put = @CachePut(value = "inventory", key = "#request.productId"),
        evict = @CacheEvict(value = "inventoryList", allEntries = true)
    )
    @Transactional
    public Optional<InventoryItem> reserveStock(InventoryRequest request) {
        String productId = request.getProductId();
        Integer quantity = request.getQuantity();

        InventoryItem item = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> 
                    new InventoryNotFoundException("Inventory item not found for product " + productId)
                );

        if (item.getAvailableQuantity() < quantity) {
            throw new InventoryNotEnoughException("Insufficient stock for product " + productId);
        }

        item.setAvailableQuantity(item.getAvailableQuantity() - quantity);
        item.setReservedQuantity(item.getReservedQuantity() + quantity);

        inventoryRepository.save(item);
        return Optional.of(item);
    }

    @Caching(
        put = @CachePut(value = "inventory", key = "#request.productId"),
        evict = @CacheEvict(value = "inventoryList", allEntries = true)
    )
    @Transactional
    public Optional<InventoryItem> deductStock(InventoryRequest request) {
        String productId = request.getProductId();
        Integer quantity = request.getQuantity();

        InventoryItem item = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> 
                    new InventoryNotFoundException("Inventory item not found for product " + productId)
                );

        item.setReservedQuantity(item.getReservedQuantity() - quantity);
        inventoryRepository.save(item);
        return Optional.of(item);
    }

    @Caching(
        put = @CachePut(value = "inventory", key = "#request.productId"),
        evict = @CacheEvict(value = "inventoryList", allEntries = true)
    )
    @Transactional
    public Optional<InventoryItem> releaseStock(InventoryRequest request) {
        String productId = request.getProductId();
        Integer quantity = request.getQuantity();

        InventoryItem item = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> 
                    new InventoryNotFoundException("Inventory item not found for product " + productId)
                );

        item.setAvailableQuantity(item.getAvailableQuantity() + quantity);
        item.setReservedQuantity(item.getReservedQuantity() - quantity);

        inventoryRepository.save(item);
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
}