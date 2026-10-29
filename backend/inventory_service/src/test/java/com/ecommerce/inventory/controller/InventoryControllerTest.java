package com.ecommerce.inventory.controller;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ecommerce.inventory.domain.InventoryItem;
import com.ecommerce.inventory.service.InventoryService;
import com.ecommerce.shared.dto.inventory.InventoryRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = InventoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventoryService inventoryService;

    @Test
    void updateStockShouldUpdateInventory() throws Exception {
        InventoryItem updatedItem = new InventoryItem("p1", 150, 0);
        when(inventoryService.updateStock(any(InventoryRequest.class)))
                .thenReturn(Optional.of(updatedItem));

        String requestJson = """
                {
                    "productId": "p1",
                    "quantity": 50
                }
                """;

        mockMvc.perform(patch("/inventory/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value("p1"))
                .andExpect(jsonPath("$.availableQuantity").value(150))
                .andExpect(jsonPath("$.reservedQuantity").value(0));
    }

    @Test
    void updateStockShouldReturn404WhenInventoryItemNotFound() throws Exception {
        when(inventoryService.updateStock(any(InventoryRequest.class)))
                .thenReturn(Optional.empty());

        String requestJson = """
                {
                    "productId": "p1",
                    "quantity": 50
                }
                """;

        mockMvc.perform(patch("/inventory/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void getInventoryItemShouldReturnInventoryItemWhenFound() throws Exception {
        InventoryItem item = new InventoryItem("p1", 100, 50);
        when(inventoryService.getInventory(anyString())).thenReturn(Optional.of(item));

        mockMvc.perform(get("/inventory/p1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value("p1"))
                .andExpect(jsonPath("$.availableQuantity").value(100))
                .andExpect(jsonPath("$.reservedQuantity").value(50));
    }

    @Test
    void getInventoryItemShouldReturn404WhenNotFound() throws Exception {
        when(inventoryService.getInventory(anyString())).thenReturn(Optional.empty());
        mockMvc.perform(get("/inventory/p1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllInventoryItemsShouldReturnAllItems() throws Exception {
        InventoryItem item1 = new InventoryItem("p1", 100, 50);
        InventoryItem item2 = new InventoryItem("p2", 200, 30);
        when(inventoryService.getAllInventory()).thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value("p1"))
                .andExpect(jsonPath("$[0].availableQuantity").value(100))
                .andExpect(jsonPath("$[0].reservedQuantity").value(50))
                .andExpect(jsonPath("$[1].productId").value("p2"))
                .andExpect(jsonPath("$[1].availableQuantity").value(200))
                .andExpect(jsonPath("$[1].reservedQuantity").value(30));
    }
}
