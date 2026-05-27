package com.ecommerce.inventory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.dto.InventoryRequest;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
    
    @PostMapping("/add")
    public ResponseEntity<InventoryItem> addStock(@RequestBody InventoryRequest request) {
        return inventoryService.addStock(request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/reserve")
    public ResponseEntity<InventoryItem> reserveStock(@RequestBody InventoryRequest request) {
        return inventoryService.reserveStock(request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/deduct")
    public ResponseEntity<InventoryItem> deductStock(@RequestBody InventoryRequest request) {
        return inventoryService.deductStock(request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/release")
    public ResponseEntity<InventoryItem> releaseStock(@RequestBody InventoryRequest request) {
        return inventoryService.releaseStock(request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryItem> getInventory(@PathVariable String productId) {
        return inventoryService.getInventory(productId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
