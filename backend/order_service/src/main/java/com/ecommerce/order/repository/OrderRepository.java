package com.ecommerce.order.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ecommerce.order.domain.Order;

public interface OrderRepository extends MongoRepository<Order, String> {
}
