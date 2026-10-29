package com.ecommerce.order.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.order.domain.Order;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.shared.dto.order.CreateOrderRequest;
import com.ecommerce.shared.dto.order.UpdateOrderRequest;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @PostMapping
    public Order createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            JwtAuthenticationToken authentication) {
        return orderService.createOrder(request, authentication);
    }

    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @GetMapping
    public List<Order> getOrders(
            JwtAuthenticationToken authentication) {
        return orderService.getOrders(authentication);
    }

    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(
            @PathVariable("id") String id,
            JwtAuthenticationToken authentication) {
        return orderService.getOrder(id, authentication)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(
            @PathVariable("id") String id,
            JwtAuthenticationToken authentication) {

        return orderService.cancelOrder(id, authentication)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<Order> updateOrderFields(
            @PathVariable("id") String id,
            @Valid @RequestBody UpdateOrderRequest request,
            JwtAuthenticationToken authentication) {

        return orderService.updateOrder(id, request, authentication)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
