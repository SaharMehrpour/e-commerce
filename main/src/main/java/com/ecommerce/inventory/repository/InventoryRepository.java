package com.ecommerce.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.inventory.domain.InventoryItem;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<InventoryItem, String> {
    Optional<InventoryItem> findByProductId(String productId);
}