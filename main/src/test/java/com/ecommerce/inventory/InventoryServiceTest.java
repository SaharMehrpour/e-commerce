package com.ecommerce.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.inventory.domain.InventoryItem;
import com.ecommerce.inventory.dto.InventoryRequest;
import com.ecommerce.inventory.messaging.InventoryEventProducer;
import com.ecommerce.inventory.repository.InventoryRepository;
import com.ecommerce.inventory.service.InventoryService;
import com.ecommerce.shared.exception.InvalidInventoryException;
import com.ecommerce.shared.exception.InventoryNotEnoughException;
import com.ecommerce.shared.exception.InventoryNotFoundException;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryEventProducer kafkaProducer;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void addStockNewShouldBeAddedToInventory() {
        when(inventoryRepository.findByProductId("product-123")).thenReturn(Optional.empty());
        when(inventoryRepository.save(any(InventoryItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryRequest request = new InventoryRequest();
        request.setProductId("product-123");
        request.setQuantity(10);

        Optional<InventoryItem> result = inventoryService.addStock(request);

        assertTrue(result.isPresent());
        InventoryItem item = result.get();
        assertEquals("product-123", item.getProductId());
        assertEquals(10, item.getAvailableQuantity());
        assertEquals(0, item.getReservedQuantity());
        verify(inventoryRepository, times(1)).save(any(InventoryItem.class));
    }

    @Test
    void addStockExistingShouldBeUpdatedInInventory() {
        InventoryItem existingItem = new InventoryItem("product-123", 10, 0);
        when(inventoryRepository.findByProductId("product-123")).thenReturn(Optional.of(existingItem));
        when(inventoryRepository.save(any(InventoryItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryRequest request = new InventoryRequest();
        request.setProductId("product-123");
        request.setQuantity(10);
        
        Optional<InventoryItem> result = inventoryService.addStock(request);

        assertTrue(result.isPresent());
        InventoryItem item = result.get();
        assertEquals("product-123", item.getProductId());
        assertEquals(20, item.getAvailableQuantity());
        assertEquals(0, item.getReservedQuantity());
        verify(inventoryRepository, times(1)).save(existingItem);
    }

    @Test
    void reserveStockShouldBeUpdatedInInventory() {
        InventoryItem existingItem = new InventoryItem("product-123", 10, 0);
        when(inventoryRepository.findByProductId("product-123")).thenReturn(Optional.of(existingItem));
        when(inventoryRepository.save(any(InventoryItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryRequest request = new InventoryRequest();
        request.setProductId("product-123");
        request.setQuantity(5);

        Optional<InventoryItem> result = inventoryService.reserveStock(request);

        assertTrue(result.isPresent());
        InventoryItem item = result.get();
        assertEquals("product-123", item.getProductId());
        assertEquals(5, item.getAvailableQuantity());
        assertEquals(5, item.getReservedQuantity());
        verify(inventoryRepository, times(1)).save(existingItem);
    }

    @Test
    void reserveStockNotEnoughShouldThrowException() {
        InventoryItem existingItem = new InventoryItem("product-123", 10, 0);
        when(inventoryRepository.findByProductId("product-123")).thenReturn(Optional.of(existingItem));

        InventoryRequest request = new InventoryRequest();
        request.setProductId("product-123");
        request.setQuantity(15);
        
        InventoryNotEnoughException ex = assertThrows(
                InventoryNotEnoughException.class,
                () -> inventoryService.reserveStock(request)
        );

        assertEquals("Insufficient stock for product product-123", ex.getMessage());
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void reserveStockNotFoundShouldThrowException() {
        when(inventoryRepository.findByProductId("product-123")).thenReturn(Optional.empty());

        InventoryRequest request = new InventoryRequest();
        request.setProductId("product-123");
        request.setQuantity(5);
        
        InventoryNotFoundException ex = assertThrows(
                InventoryNotFoundException.class,
                () -> inventoryService.reserveStock(request)
        );
        
        assertEquals("Inventory item not found for product product-123", ex.getMessage());
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void deductStockShouldBeUpdatedInInventory() {
        InventoryItem existingItem = new InventoryItem("product-123", 10, 5);
        when(inventoryRepository.findByProductId("product-123")).thenReturn(Optional.of(existingItem));
        when(inventoryRepository.save(any(InventoryItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryRequest request = new InventoryRequest();
        request.setProductId("product-123");
        request.setQuantity(5);

        Optional<InventoryItem> result = inventoryService.deductStock(request);

        assertTrue(result.isPresent());
        InventoryItem item = result.get();
        assertEquals("product-123", item.getProductId());
        assertEquals(10, item.getAvailableQuantity());
        assertEquals(0, item.getReservedQuantity());
        verify(inventoryRepository, times(1)).save(existingItem);
    }

    @Test
    void deductStockNotFoundShouldThrowException() {
        when(inventoryRepository.findByProductId("product-123")).thenReturn(Optional.empty());

        InventoryRequest request = new InventoryRequest();
        request.setProductId("product-123");
        request.setQuantity(5);

        InventoryNotFoundException ex = assertThrows(
                InventoryNotFoundException.class,
                () -> inventoryService.deductStock(request)
        );

        assertEquals("Inventory item not found for product product-123", ex.getMessage());
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void deductStockShouldThrowWhenReservedQuantityIsTooLow() {
        InventoryItem existingItem = new InventoryItem("product-123", 10, 2);
        when(inventoryRepository.findByProductId("product-123")).thenReturn(Optional.of(existingItem));

        InventoryRequest request = new InventoryRequest();
        request.setProductId("product-123");
        request.setQuantity(5);

        InventoryNotEnoughException ex = assertThrows(
                InventoryNotEnoughException.class,
                () -> inventoryService.deductStock(request)
        );

        assertEquals("Reserved stock is less than requested quantity for product product-123", ex.getMessage());
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void releaseStockShouldBeUpdatedInInventory() {
        InventoryItem existingItem = new InventoryItem("product-123", 10, 5);
        when(inventoryRepository.findByProductId("product-123")).thenReturn(Optional.of(existingItem));
        when(inventoryRepository.save(any(InventoryItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryRequest request = new InventoryRequest();
        request.setProductId("product-123");
        request.setQuantity(5);

        Optional<InventoryItem> result = inventoryService.releaseStock(request);
        
        assertTrue(result.isPresent());
        InventoryItem item = result.get();
        assertEquals("product-123", item.getProductId());
        assertEquals(15, item.getAvailableQuantity());
        assertEquals(0, item.getReservedQuantity());
        verify(inventoryRepository, times(1)).save(existingItem);
    }

    @Test
    void releaseStockNotFoundShouldThrowException() {
        when(inventoryRepository.findByProductId("product-123")).thenReturn(Optional.empty());

        InventoryRequest request = new InventoryRequest();
        request.setProductId("product-123");
        request.setQuantity(5);

        InventoryNotFoundException ex = assertThrows(
                InventoryNotFoundException.class,
                () -> inventoryService.releaseStock(request)
        );
        
        assertEquals("Inventory item not found for product product-123", ex.getMessage());
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void releaseStockShouldThrowWhenReservedQuantityIsTooLow() {
        InventoryItem existingItem = new InventoryItem("product-123", 10, 2);
        when(inventoryRepository.findByProductId("product-123")).thenReturn(Optional.of(existingItem));

        InventoryRequest request = new InventoryRequest();
        request.setProductId("product-123");
        request.setQuantity(5);

        InventoryNotEnoughException ex = assertThrows(
                InventoryNotEnoughException.class,
                () -> inventoryService.releaseStock(request)
        );

        assertEquals("Reserved stock is less than requested quantity for product product-123", ex.getMessage());
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void addStockShouldRejectInvalidRequest() {
        InventoryRequest request = new InventoryRequest();
        request.setProductId(" ");
        request.setQuantity(5);

        InvalidInventoryException ex = assertThrows(
                InvalidInventoryException.class,
                () -> inventoryService.addStock(request)
        );

        assertEquals("Product ID is required", ex.getMessage());
        verifyNoInteractions(inventoryRepository);
    }

    @Test
    void getInventoryItemShouldReturnItem() {
        InventoryItem existingItem = new InventoryItem("product-123", 10, 5);
        when(inventoryRepository.findByProductId("product-123")).thenReturn(Optional.of(existingItem));

        Optional<InventoryItem> result = inventoryService.getInventory("product-123");

        assertTrue(result.isPresent());
        InventoryItem item = result.get();
        assertEquals("product-123", item.getProductId());
        assertEquals(10, item.getAvailableQuantity());
        assertEquals(5, item.getReservedQuantity());
    }

    @Test
    void getInventoryItemNotFoundShouldReturnEmpty() {
        when(inventoryRepository.findByProductId("product-123")).thenReturn(Optional.empty());

        Optional<InventoryItem> result = inventoryService.getInventory("product-123");
        
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllInventoryShouldReturnList() {
        InventoryItem item1 = new InventoryItem("product-123", 10, 5);
        InventoryItem item2 = new InventoryItem("product-456", 20, 10);
        List<InventoryItem> expectedItems = List.of(item1, item2);
        when(inventoryRepository.findAll()).thenReturn(expectedItems);

        var result = inventoryService.getAllInventory();

        assertEquals(2, result.size());
        assertTrue(result.contains(item1));
        assertTrue(result.contains(item2));
        verify(inventoryRepository, times(1)).findAll();
    }

    @Test
    void validateInventoryRequestShouldThrowWhenRequestIsNull() {
        InvalidInventoryException ex = assertThrows(
                InvalidInventoryException.class,
                () -> inventoryService.addStock(null)
        );

        assertEquals("Inventory request is required", ex.getMessage());
        verifyNoInteractions(inventoryRepository);
    }

    @Test
    void validateInventoryRequestShouldThrowWhenProductIdIsBlank() {
        InventoryRequest request = new InventoryRequest();
        request.setProductId(" ");
        request.setQuantity(5); 

        InvalidInventoryException ex = assertThrows(
                InvalidInventoryException.class,
                () -> inventoryService.addStock(request)
        );
        
        assertEquals("Product ID is required", ex.getMessage());
        verifyNoInteractions(inventoryRepository);
    }

    @Test
    void validateInventoryRequestShouldThrowWhenQuantityIsInvalid() {
        InventoryRequest request = new InventoryRequest();
        request.setProductId("product-123");
        request.setQuantity(0);
        
        InvalidInventoryException ex = assertThrows(
                InvalidInventoryException.class,
                () -> inventoryService.addStock(request)
        );
        
        assertEquals("Quantity must be greater than zero", ex.getMessage());
        verifyNoInteractions(inventoryRepository);
    }
}
