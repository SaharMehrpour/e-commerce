package com.ecommerce.order.service;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.ecommerce.order.domain.Order;
import com.ecommerce.order.repository.OrderRepository;

@Service
public class OrderCacheService {

    private final OrderRepository repository;

    public OrderCacheService(OrderRepository repository) {
        this.repository = repository;
    }

    @Cacheable(value = "orders", key = "#id")
    public Optional<Order> getOrder(String id) {
        return repository.findById(id);
    }

    @Cacheable(value = "orders", key = "'all'")
    public List<Order> getAllOrders() {
        return repository.findAll();
    }

    @Cacheable(value = "orders", key = "'user:' + #userId")
    public List<Order> getOrdersForUser(String userId) {
        return repository.findByUserId(userId);
    }

}