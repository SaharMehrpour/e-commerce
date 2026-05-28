package com.ecommerce.inventory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.ecommerce.dto.InventoryRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@WebMvcTest(InventoryController.class)
public class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private TestInventoryService inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService.inventoryItems = new ArrayList<>();
        inventoryService.inventoryItem = Optional.empty();
    }

    @Test
    void shouldAddStockToExistingOne() throws Exception {
        InventoryItem item = new InventoryItem("p1", 100, 0);
        inventoryService.inventoryItem = Optional.of(item);

        String requestJson = """
            {
                "productId": "p1",
                "quantity": 50
            }
            """;
        
        mockMvc.perform(patch("/inventory/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value("p1"))
                .andExpect(jsonPath("$.availableQuantity").value(150))
                .andExpect(jsonPath("$.reservedQuantity").value(0));
    }

    @Test
    void shouldAddStockAsNewItem() throws Exception {
        String requestJson = """
            {
                "productId": "p1",
                "quantity": 50
            }
            """;
        
        mockMvc.perform(patch("/inventory/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value("p1"))
                .andExpect(jsonPath("$.availableQuantity").value(50))
                .andExpect(jsonPath("$.reservedQuantity").value(0));
    }

    @Test
    void shouldReturn404WhenInventoryItemNotFound() throws Exception {
        String requestJson = """
            {
                "productId": "p1",
                "quantity": 50
            }
            """;
        
        mockMvc.perform(patch("/inventory/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReserveStock() throws Exception {
        InventoryItem item = new InventoryItem("p1", 100, 0);
        inventoryService.inventoryItem = Optional.of(item);

        String requestJson = """
            {
                "productId": "p1",
                "quantity": 50
            }
            """;
        
            mockMvc.perform(patch("/inventory/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value("p1"))
                .andExpect(jsonPath("$.availableQuantity").value(50))
                .andExpect(jsonPath("$.reservedQuantity").value(50));
    }

    @Test
    void shouldDeductStock() throws Exception {
        InventoryItem item = new InventoryItem("p1", 100, 50);
        inventoryService.inventoryItem = Optional.of(item);

        String requestJson = """
            {
                "productId": "p1",
                "quantity": 30
            }
            """;
        
            mockMvc.perform(patch("/inventory/deduct")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value("p1"))
                .andExpect(jsonPath("$.availableQuantity").value(100))
                .andExpect(jsonPath("$.reservedQuantity").value(20));
    }

    @Test
    void shouldReleaseStock() throws Exception {
        InventoryItem item = new InventoryItem("p1", 100, 50);
        inventoryService.inventoryItem = Optional.of(item);

        String requestJson = """
            {
                "productId": "p1",
                "quantity": 30
            }
            """;
        
            mockMvc.perform(patch("/inventory/release")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value("p1"))
                .andExpect(jsonPath("$.availableQuantity").value(130))
                .andExpect(jsonPath("$.reservedQuantity").value(20));
    }

    @Test
    void shouldReturnInventoryItemWhenFound() throws Exception {
        InventoryItem item = new InventoryItem("p1", 100, 50);
        inventoryService.inventoryItem = Optional.of(item);
        
        mockMvc.perform(get("/inventory/p1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value("p1"))
            .andExpect(jsonPath("$.availableQuantity").value(100))
            .andExpect(jsonPath("$.reservedQuantity").value(50));
    }

    @Test
    void shouldUpdateInventoryItemWhenFound() throws Exception {
        InventoryItem item = new InventoryItem("p1", 100, 50);
        inventoryService.inventoryItem = Optional.of(item);

        String requestJson = """
            {
                "productId": "p1",
                "availableQuantity": 80,
                "reservedQuantity": 30
            }
            """;
        
        mockMvc.perform(patch("/inventory/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value("p1"))
                .andExpect(jsonPath("$.availableQuantity").value(80))
                .andExpect(jsonPath("$.reservedQuantity").value(30));
    }

    @Test
    void shouldReturn404WhenUpdatingInventoryItemNotFound() throws Exception {
        String requestJson = """
            {
                "productId": "p1",
                "availableQuantity": 80,
                "reservedQuantity": 30
            }
            """;
        
        mockMvc.perform(patch("/inventory/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnAllInventoryItems() throws Exception {
        InventoryItem item1 = new InventoryItem("p1", 100, 50);
        InventoryItem item2 = new InventoryItem("p2", 200, 30);
        inventoryService.inventoryItems = List.of(item1, item2);
        
        mockMvc.perform(get("/inventory"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].productId").value("p1"))
            .andExpect(jsonPath("$[0].availableQuantity").value(100))
            .andExpect(jsonPath("$[0].reservedQuantity").value(50))
            .andExpect(jsonPath("$[1].productId").value("p2"))
            .andExpect(jsonPath("$[1].availableQuantity").value(200))
            .andExpect(jsonPath("$[1].reservedQuantity").value(30));
    }
    

    @TestConfiguration
    static class TestConfig {

        @Bean
        TestInventoryService inventoryService() {
            return new TestInventoryService();
        }
    }

    static class TestInventoryService extends InventoryService {

        private InventoryItem inventoryItemToUpdate;
        public List<InventoryItem> inventoryItems = new ArrayList<>();
        private Optional<InventoryItem> inventoryItem = Optional.empty();

        public TestInventoryService() {
            super(null);
        }

        @Override
        public Optional<InventoryItem> addStock(InventoryRequest request) {
            if (inventoryItem.isPresent() && inventoryItem.get().getProductId().equals(request.getProductId())) {
                inventoryItemToUpdate = inventoryItem.get();
                inventoryItemToUpdate.setAvailableQuantity(inventoryItemToUpdate.getAvailableQuantity() + request.getQuantity());
                return Optional.of(inventoryItemToUpdate);
            } else {
                InventoryItem newItem = new InventoryItem(request.getProductId(), request.getQuantity(), 0);
                inventoryItems.add(newItem);
                return Optional.of(newItem);
            }
        }    
        
        @Override
        public Optional<InventoryItem> reserveStock(InventoryRequest request) {
            if (inventoryItem.isPresent() && inventoryItem.get().getProductId().equals(request.getProductId())) {
                inventoryItemToUpdate = inventoryItem.get();
                inventoryItemToUpdate.setAvailableQuantity(inventoryItemToUpdate.getAvailableQuantity() - request.getQuantity());
                inventoryItemToUpdate.setReservedQuantity(inventoryItemToUpdate.getReservedQuantity() + request.getQuantity());
                return Optional.of(inventoryItemToUpdate);
            } else {
                return Optional.empty();
            }
        }

        @Override
        public Optional<InventoryItem> deductStock(InventoryRequest request) {
            if (inventoryItem.isPresent() && inventoryItem.get().getProductId().equals(request.getProductId())) {
                inventoryItemToUpdate = inventoryItem.get();
                inventoryItemToUpdate.setReservedQuantity(inventoryItemToUpdate.getReservedQuantity() - request.getQuantity());
                return Optional.of(inventoryItemToUpdate);
            } else {
                return Optional.empty();
            }
        }

        @Override
        public Optional<InventoryItem> releaseStock(InventoryRequest request) {
            if (inventoryItem.isPresent() && inventoryItem.get().getProductId().equals(request.getProductId())) {
                inventoryItemToUpdate = inventoryItem.get();
                inventoryItemToUpdate.setAvailableQuantity(inventoryItemToUpdate.getAvailableQuantity() + request.getQuantity());
                inventoryItemToUpdate.setReservedQuantity(inventoryItemToUpdate.getReservedQuantity() - request.getQuantity());
                return Optional.of(inventoryItemToUpdate);
            } else {
                return Optional.empty();
            }
        }

        @Override
        public Optional<InventoryItem> getInventory(String productId) {
            if (inventoryItem.isPresent() && inventoryItem.get().getProductId().equals(productId)) {
                return inventoryItem;
            } else {
                return Optional.empty();
            }
        }

        @Override
        public Optional<InventoryItem> updateInventory(InventoryRequest request) {
            String productId = request.getProductId();
            if (inventoryItem.isPresent() && inventoryItem.get().getProductId().equals(productId)) {
                inventoryItemToUpdate = inventoryItem.get();
                if (request.getAvailableQuantity() != null) {
                    inventoryItemToUpdate.setAvailableQuantity(request.getAvailableQuantity());
                }
                if (request.getReservedQuantity() != null) {
                    inventoryItemToUpdate.setReservedQuantity(request.getReservedQuantity());
                }
                return Optional.of(inventoryItemToUpdate);
            }
            return Optional.empty();
        }

        @Override
        public List<InventoryItem> getAllInventory() {
            return inventoryItems;
        }
    }
}
