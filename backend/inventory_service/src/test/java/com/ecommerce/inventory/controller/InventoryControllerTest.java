package com.ecommerce.inventory.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ecommerce.inventory.domain.InventoryItem;
import com.ecommerce.inventory.idempotency.ProcessedEventRepository;
import com.ecommerce.inventory.service.InventoryService;
import com.ecommerce.shared.dto.inventory.InventoryRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;

@WebMvcTest(controllers = InventoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = InventoryController.class)

public class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventoryService inventoryService;

    @MockitoBean
    ProcessedEventRepository processedEventRepository;

    @Test
    void addStockShouldUpdateInventory() throws Exception {
        InventoryItem updatedItem = new InventoryItem("p1", 150, 0);
        when(inventoryService.addStock(any(InventoryRequest.class))).thenReturn(Optional.of(updatedItem));

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
    void addStockShouldReturn404WhenInventoryItemNotFound() throws Exception {
        when(inventoryService.addStock(any(InventoryRequest.class))).thenReturn(Optional.empty());
        String requestJson = """
                {
                    "productId": "p1",
                    "quantity": 50
                }
                """;

        mockMvc.perform(patch("/inventory/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void reserveStockShouldUpdateInventory() throws Exception {
        InventoryItem updatedItem = new InventoryItem("p1", 50, 50);
        when(inventoryService.reserveStock(any(InventoryRequest.class))).thenReturn(Optional.of(updatedItem));

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
    void reserveStockShouldReturn404ForNonExistingItem() throws Exception {
        when(inventoryService.reserveStock(any(InventoryRequest.class))).thenReturn(Optional.empty());
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
    void deductStockShouldUpdateInventory() throws Exception {
        InventoryItem updatedItem = new InventoryItem("p1", 70, 50);
        when(inventoryService.deductStock(any(InventoryRequest.class))).thenReturn(Optional.of(updatedItem));

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
                .andExpect(jsonPath("$.availableQuantity").value(70))
                .andExpect(jsonPath("$.reservedQuantity").value(50));
    }

    @Test
    void deductStockShouldReturn404ForNonExistingItem() throws Exception {
        when(inventoryService.deductStock(any(InventoryRequest.class))).thenReturn(Optional.empty());
        String requestJson = """
                {
                    "productId": "p1",
                    "quantity": 30
                }
                """;

        mockMvc.perform(patch("/inventory/deduct")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void releaseStockShouldUpdateInventory() throws Exception {
        InventoryItem updatedItem = new InventoryItem("p1", 130, 20);
        when(inventoryService.releaseStock(any(InventoryRequest.class))).thenReturn(Optional.of(updatedItem));

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
    void releaseStockShouldReturn404ForNonExistingItem() throws Exception {
        when(inventoryService.releaseStock(any(InventoryRequest.class))).thenReturn(Optional.empty());
        String requestJson = """
                {
                    "productId": "p1",
                    "quantity": 30
                }
                """;

        mockMvc.perform(patch("/inventory/release")
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

// package com.ecommerce.inventory.controller;

// import com.ecommerce.inventory.domain.InventoryItem;
// import com.ecommerce.inventory.service.InventoryService;
// import com.ecommerce.shared.dto.inventory.InventoryRequest;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
// import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
// import org.springframework.http.MediaType;
// import org.springframework.test.context.ContextConfiguration;
// import org.springframework.test.context.bean.override.mockito.MockitoBean;
// import org.springframework.test.web.servlet.MockMvc;

// import java.util.Optional;

// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.when;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest(InventoryController.class)
// @AutoConfigureMockMvc(addFilters = false)
// @ContextConfiguration(classes = InventoryController.class)
// class InventoryControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @MockitoBean
//     private InventoryService inventoryService;

//     @Test
//     void addStock_shouldReturnUpdatedInventory() throws Exception {

//         InventoryItem responseItem = new InventoryItem("p1", 150, 0);

//         when(inventoryService.addStock(any(InventoryRequest.class)))
//                 .thenReturn(Optional.of(responseItem));

//         String requestJson = """
//             {
//                 "productId": "p1",
//                 "quantity": 50
//             }
//         """;

//         mockMvc.perform(patch("/inventory/add")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(requestJson))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.productId").value("p1"))
//                 .andExpect(jsonPath("$.availableQuantity").value(150))
//                 .andExpect(jsonPath("$.reservedQuantity").value(0));
//     }
// }