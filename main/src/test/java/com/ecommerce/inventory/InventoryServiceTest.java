package com.ecommerce.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ecommerce.dto.InventoryRequest;
import com.ecommerce.exception.InventoryNotEnoughException;
import com.ecommerce.exception.InventoryNotFoundException;

public class InventoryServiceTest {

    private FakeInventoryRepository inventoryRepository;
    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        inventoryRepository = new FakeInventoryRepository();
        inventoryService = new InventoryService(inventoryRepository.proxy());
    }

    @Test
    void addStockNewShouldBeAddedToInventory() {
        InventoryRequest request = new InventoryRequest();
        request.setProductId("product-123");
        request.setQuantity(10);
        Optional<InventoryItem> result = inventoryService.addStock(request);

        assertTrue(result.isPresent());
        InventoryItem item = result.get();
        assertEquals("product-123", item.getProductId());
        assertEquals(10, item.getAvailableQuantity());
        assertEquals(0, item.getReservedQuantity());
    }

    @Test
    void addStockExistingShouldBeUpdatedInInventory() {
        InventoryItem existingItem = new InventoryItem("product-123", 10, 0);
        inventoryRepository.itemById = Optional.of(existingItem);
        InventoryRequest request = new InventoryRequest();
        request.setProductId("product-123");
        request.setQuantity(10);
        Optional<InventoryItem> result = inventoryService.addStock(request);

        assertTrue(result.isPresent());
        InventoryItem item = result.get();
        assertEquals("product-123", item.getProductId());
        assertEquals(20, item.getAvailableQuantity());
        assertEquals(0, item.getReservedQuantity());
    }

    @Test
    void reserveStockShouldBeUpdatedInInventory() {
        InventoryItem existingItem = new InventoryItem("product-123", 10, 0);
        inventoryRepository.itemById = Optional.of(existingItem);
        InventoryRequest request = new InventoryRequest();
        request.setProductId("product-123");
        request.setQuantity(5);

        Optional<InventoryItem> result = inventoryService.reserveStock(request);

        assertTrue(result.isPresent());
        InventoryItem item = result.get();
        assertEquals("product-123", item.getProductId());
        assertEquals(5, item.getAvailableQuantity());
        assertEquals(5, item.getReservedQuantity());
    }

    @Test
    void reserveStockNotEnoughShouldThrowException() {
        InventoryItem existingItem = new InventoryItem("product-123", 10, 0);
        inventoryRepository.itemById = Optional.of(existingItem);
        InventoryRequest request = new InventoryRequest();
        request.setProductId("product-123");
        request.setQuantity(15);
        
        InventoryNotEnoughException ex = assertThrows(
                InventoryNotEnoughException.class,
                () -> inventoryService.reserveStock(request)
        );

        assertEquals("Insufficient stock for product product-123", ex.getMessage());
    }

    @Test
    void reserveStockNotFoundShouldThrowException() {
        InventoryRequest request = new InventoryRequest();
        request.setProductId("product-123");
        request.setQuantity(5);
        InventoryNotFoundException ex = assertThrows(
                InventoryNotFoundException.class,
                () -> inventoryService.reserveStock(request)
        );
        assertEquals("Inventory item not found for product product-123", ex.getMessage());
    }

    @Test
    void deductStockShouldBeUpdatedInInventory() {
        InventoryItem existingItem = new InventoryItem("product-123", 10, 5);
        inventoryRepository.itemById = Optional.of(existingItem);
        InventoryRequest request = new InventoryRequest();
        request.setProductId("product-123");
        request.setQuantity(5);

        Optional<InventoryItem> result = inventoryService.deductStock(request);

        assertTrue(result.isPresent());
        InventoryItem item = result.get();
        assertEquals("product-123", item.getProductId());
        assertEquals(10, item.getAvailableQuantity());
        assertEquals(0, item.getReservedQuantity());
    }

    @Test
    void deductStockNotFoundShouldThrowException() {
        InventoryRequest request = new InventoryRequest();
        request.setProductId("product-123");
        request.setQuantity(5);

        InventoryNotFoundException ex = assertThrows(
                InventoryNotFoundException.class,
                () -> inventoryService.deductStock(request)
        );

        assertEquals("Inventory item not found for product product-123", ex.getMessage());
    }

    @Test
    void releaseStockShouldBeUpdatedInInventory() {
        InventoryItem existingItem = new InventoryItem("product-123", 10, 5);
        inventoryRepository.itemById = Optional.of(existingItem);
        InventoryRequest request = new InventoryRequest();
        request.setProductId("product-123");
        request.setQuantity(5);

        Optional<InventoryItem> result = inventoryService.releaseStock(request);
        
        assertTrue(result.isPresent());
        InventoryItem item = result.get();
        assertEquals("product-123", item.getProductId());
        assertEquals(15, item.getAvailableQuantity());
        assertEquals(0, item.getReservedQuantity());
    }

    @Test
    void releaseStockNotFoundShouldThrowException() {
        InventoryRequest request = new InventoryRequest();
        request.setProductId("product-123");
        request.setQuantity(5);

        InventoryNotFoundException ex = assertThrows(
                InventoryNotFoundException.class,
                () -> inventoryService.releaseStock(request)
        );
        
        assertEquals("Inventory item not found for product product-123", ex.getMessage());
    }

    @Test
    void getInventoryItemShouldReturnItem() {
        InventoryItem existingItem = new InventoryItem("product-123", 10, 5);
        inventoryRepository.itemById = Optional.of(existingItem);

        Optional<InventoryItem> result = inventoryService.getInventory("product-123");

        assertTrue(result.isPresent());
        InventoryItem item = result.get();
        assertEquals("product-123", item.getProductId());
        assertEquals(10, item.getAvailableQuantity());
        assertEquals(5, item.getReservedQuantity());
    }
    

    private static class FakeInventoryRepository {

        private InventoryItem savedItem;
        private Optional<InventoryItem> itemById = Optional.empty();

        private InventoryRepository proxy() {
            return (InventoryRepository) Proxy.newProxyInstance(
                    InventoryRepository.class.getClassLoader(),
                    new Class<?>[] { InventoryRepository.class },
                    (proxy, method, args) -> {
                        String methodName = method.getName();

                        if ("findByProductId".equals(methodName)) {
                            return itemById;
                        } else if ("save".equals(methodName)) {
                            savedItem = (InventoryItem) args[0];
                            return savedItem;
                        }
                        throw new UnsupportedOperationException("Method not implemented: " + methodName);
                    });
        }
    }
    
}
