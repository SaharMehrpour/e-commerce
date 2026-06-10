package com.ecommerce.order.service;

import com.ecommerce.order.client.InventoryClient;
import com.ecommerce.order.domain.Order;
import com.ecommerce.order.messaging.OrderEventProducer;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.shared.domain.order.OrderStatus;
import com.ecommerce.shared.dto.inventory.InventoryResponse;
import com.ecommerce.shared.dto.order.CreateOrderRequest;
import com.ecommerce.shared.dto.order.UpdateOrderRequest;
import com.ecommerce.shared.event.order.OrderCancelledEvent;
import com.ecommerce.shared.event.order.OrderCreatedEvent;
import com.ecommerce.shared.event.order.OrderUpdatedEvent;
import com.ecommerce.shared.exception.inventory.InventoryNotEnoughException;
import com.ecommerce.shared.exception.inventory.InventoryNotFoundException;
import com.ecommerce.shared.exception.order.OrderNotFoundException;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository repository;

    @Mock
    private OrderEventProducer kafkaProducer;

    @Mock
    private InventoryClient inventoryClient;

    private MeterRegistry meterRegistry;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();

        orderService = new OrderService(
                repository,
                kafkaProducer,
                inventoryClient,
                meterRegistry);
    }

    @Test
    void createOrderShouldSaveOrderAndPublishEvent() {

        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId("u1");
        request.setProductId("p1");
        request.setQuantity(2);

        InventoryResponse mockInventory = new InventoryResponse();
        mockInventory.setProductId("p1");
        mockInventory.setAvailableQuantity(10);

        Order mockSavedOrder = new Order();
        mockSavedOrder.setId("order-1");
        mockSavedOrder.setUserId("u1");
        mockSavedOrder.setProductId("p1");
        mockSavedOrder.setQuantity(2);
        mockSavedOrder.setStatus(OrderStatus.CREATED);

        when(inventoryClient.getInventory("p1")).thenReturn(mockInventory);
        when(repository.save(any(Order.class))).thenReturn(mockSavedOrder);

        Order createdOrder = orderService.createOrder(request);

        assertNotNull(createdOrder);
        assertEquals("order-1", createdOrder.getId());
        assertEquals(OrderStatus.CREATED, createdOrder.getStatus());

        verify(repository, times(1)).save(any(Order.class));

        ArgumentCaptor<OrderCreatedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);

        verify(kafkaProducer, times(1))
                .sendOrderCreatedEvent(eventCaptor.capture());

        OrderCreatedEvent sentEvent = eventCaptor.getValue();

        assertEquals("order-1", sentEvent.getOrderId());
        assertEquals("u1", sentEvent.getUserId());
        assertEquals("p1", sentEvent.getProductId());
        assertEquals(2, sentEvent.getQuantity());
        assertEquals(OrderStatus.CREATED, sentEvent.getStatus());
    }

    @Test
    void createOrderShouldThrowWhenInventoryMissing() {

        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId("u1");
        request.setProductId("p1");
        request.setQuantity(2);

        when(inventoryClient.getInventory("p1")).thenReturn(null);

        InventoryNotFoundException ex = assertThrows(
                InventoryNotFoundException.class,
                () -> orderService.createOrder(request));

        assertEquals("Inventory item not found for product p1", ex.getMessage());

        verify(repository, never()).save(any());
        verifyNoInteractions(kafkaProducer);
    }

    @Test
    void createOrderShouldThrowWhenNotEnoughStock() {

        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId("u1");
        request.setProductId("p1");
        request.setQuantity(5);

        InventoryResponse mockInventory = new InventoryResponse();
        mockInventory.setAvailableQuantity(2);

        when(inventoryClient.getInventory("p1")).thenReturn(mockInventory);

        InventoryNotEnoughException ex = assertThrows(
                InventoryNotEnoughException.class,
                () -> orderService.createOrder(request));

        assertEquals("Insufficient stock", ex.getMessage());

        verifyNoInteractions(repository);
        verifyNoInteractions(kafkaProducer);
    }

    @Test
    void getOrderShouldReturnRepositoryOrderById() {
        Order order = new Order();
        when(repository.findById("order-1")).thenReturn(Optional.of(order));

        Optional<Order> foundOrder = orderService.getOrder("order-1");

        assertTrue(foundOrder.isPresent());
        verify(repository).findById("order-1");
    }

    @Test
    void getOrdersShouldReturnRepositoryOrders() {

        Order first = new Order();
        Order second = new Order();

        when(repository.findAll()).thenReturn(List.of(first, second));

        List<Order> orders = orderService.getOrders();

        assertEquals(2, orders.size());
        verify(repository).findAll();
    }

    @Test
    void cancelOrderShouldPersistCancelledStatus() {

        Order order = new Order();
        order.setId("order-1");
        order.setUserId("u1");
        order.setProductId("p1");
        order.setQuantity(2);
        order.setStatus(OrderStatus.CREATED);

        when(repository.findById("order-1")).thenReturn(Optional.of(order));
        when(repository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Optional<Order> result = orderService.cancelOrder("order-1");

        assertTrue(result.isPresent());
        assertEquals(OrderStatus.CANCELLED, result.get().getStatus());

        verify(repository).save(order);

        verify(kafkaProducer).sendOrderCancelledEvent(any(OrderCancelledEvent.class));
    }

    @Test
    void updateOrderShouldUpdateFields() {

        Order order = new Order();
        order.setId("order-1");
        order.setProductId("p1");
        order.setQuantity(2);
        order.setStatus(OrderStatus.CREATED);

        when(repository.findById("order-1")).thenReturn(Optional.of(order));
        when(repository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setProductId("new-p");
        request.setQuantity(10);

        Optional<Order> updated = orderService.updateOrder("order-1", request);

        assertTrue(updated.isPresent());

        verify(kafkaProducer).sendOrderUpdatedEvent(any(OrderUpdatedEvent.class));
    }

    @Test
    void updateOrderShouldThrowWhenRequestNull() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> orderService.updateOrder("order-1", null));

        assertEquals("Request cannot be null", ex.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    void updateOrderShouldThrowWhenOrderNotFound() {

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setProductId("new-p");
        request.setQuantity(10);

        OrderNotFoundException ex = assertThrows(
                OrderNotFoundException.class,
                () -> orderService.updateOrder("order-1", request));

        assertEquals("Order not found", ex.getMessage());
        verify(repository).findById("order-1");
    }
}