package com.ecommerce.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.InventoryRequest;
import com.ecommerce.dto.UpdateOrderRequest;
import com.ecommerce.event.*;
import com.ecommerce.exception.InvalidOrderException;
import com.ecommerce.exception.InventoryNotEnoughException;
import com.ecommerce.exception.InventoryNotFoundException;
import com.ecommerce.exception.OrderAlreadyCancelledException;
import com.ecommerce.exception.OrderNotFoundException;
import com.ecommerce.exception.OrderNotUpdatableException;
import com.ecommerce.inventory.InventoryItem;
import com.ecommerce.inventory.InventoryService;
import com.ecommerce.kafka.producer.OrderKafkaProducer;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    private FakeOrderRepository repository;
    private FakeOrderKafkaProducer kafkaProducer;
    private FakeInventoryService inventoryService;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        repository = new FakeOrderRepository();
        kafkaProducer = new FakeOrderKafkaProducer();
        inventoryService = new FakeInventoryService();
        orderService = new OrderService(repository.proxy(), kafkaProducer, inventoryService);
    }

    @Test
    void createOrderShouldSaveOrderAndPublishEvent() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId("u1");
        request.setProductId("p1");
        request.setQuantity(2);
        inventoryService.inventoryItem = new InventoryItem("p1", 10, 0);

        Order createdOrder = orderService.createOrder(request);

        assertEquals("order-1", createdOrder.getId());
        assertEquals("CREATED", createdOrder.getStatus());
        assertSame(createdOrder, repository.savedOrder);

        assertInstanceOf(
                OrderCreatedEvent.class,
                kafkaProducer.sentEvent
        );
        OrderCreatedEvent event = (OrderCreatedEvent) kafkaProducer.sentEvent;

        assertNotNull(event);
        assertEquals("order-1", event.getOrderId());
        assertEquals("u1", event.getUserId());
        assertEquals("p1", event.getProductId());
        assertEquals(2, event.getQuantity());
        assertEquals("CREATED", event.getStatus());
    }

    @Test
    void createOrderShouldThrowWhenUserIdIsMissing() {

        CreateOrderRequest request = new CreateOrderRequest();
        request.setProductId("p1");
        request.setQuantity(2);

        InvalidOrderException ex = assertThrows(
                InvalidOrderException.class,
                () -> orderService.createOrder(request)
        );

        assertEquals("User ID is required", ex.getMessage());
    }

    @Test
    void createOrderShouldThrowWhenProductIdIsMissing() {

        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId("u1");
        request.setQuantity(2);
    
        InvalidOrderException ex = assertThrows(
                InvalidOrderException.class,
                () -> orderService.createOrder(request)
        );

        assertEquals("Product ID is required", ex.getMessage());
    }

    @Test
    void createOrderShouldThrowWhenQuantityIsInvalid() {

        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId("u1");
        request.setProductId("p1");
        request.setQuantity(0);

        InvalidOrderException ex = assertThrows(
                InvalidOrderException.class,
                () -> orderService.createOrder(request)
        );

        assertEquals(
                "Quantity must be greater than zero",
                ex.getMessage()
        );
    }

    @Test
    void createOrderShouldThrowWhenProductNotFound() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId("u1");
        request.setProductId("p1");
        request.setQuantity(2);

        InventoryNotFoundException ex = assertThrows(
                InventoryNotFoundException.class,
                () -> orderService.createOrder(request)
        );

        assertEquals("Inventory item not found for product p1", ex.getMessage());
    }

    @Test
    void createOrderShouldThrowWhenQuantityIsNull() {

        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId("u1");
        request.setProductId("p1");
        request.setQuantity(null);

        InvalidOrderException ex = assertThrows(
                InvalidOrderException.class,
                () -> orderService.createOrder(request));

        assertEquals("Quantity must be greater than zero", ex.getMessage());
    }

    @Test
    void getOrderShouldReturnRepositoryOrderById() {
        Order order = new Order("u1", "p1", 2);
        repository.orderById = Optional.of(order);

        Optional<Order> foundOrder = orderService.getOrder("order-1");

        assertTrue(foundOrder.isPresent());
        assertSame(order, foundOrder.get());
        assertEquals("order-1", repository.requestedId);
    }

    @Test
    void getOrderShouldReturnNotPresentForNonExistentOrder() {
        Optional<Order> foundOrder = orderService.getOrder("non-existent-id");
        assertFalse(foundOrder.isPresent());
    }

    @Test
    void getOrdersShouldReturnRepositoryOrders() {
        Order firstOrder = new Order("u1", "p1", 2);
        Order secondOrder = new Order("u2", "p2", 4);
        repository.orders = List.of(firstOrder, secondOrder);

        List<Order> orders = orderService.getOrders();

        assertEquals(List.of(firstOrder, secondOrder), orders);
    }

    @Test
    void cancelOrderShouldPersistCancelledStatus() {

        Order order = new Order("u1", "p1", 2);
        order.setId("order-1");
        order.setStatus("CREATED");

        inventoryService.inventoryItem = new InventoryItem("p1", 10, 2);

        repository.orderById = Optional.of(order);

        Optional<Order> cancelledOrder = orderService.cancelOrder("order-1");

        assertTrue(cancelledOrder.isPresent());
        assertNotNull(repository.savedOrder);
        assertEquals("CANCELLED", repository.savedOrder.getStatus());
        assertEquals("order-1", repository.savedOrder.getId());

        assertTrue(cancelledOrder.isPresent());
        Order result = cancelledOrder.get();

        assertEquals("CANCELLED", result.getStatus());
        assertInstanceOf(
                OrderCancelledEvent.class,
                kafkaProducer.sentEvent
        );

        OrderCancelledEvent event =
                (OrderCancelledEvent) kafkaProducer.sentEvent;

        assertEquals("order-1", event.getOrderId());
        assertEquals("u1", event.getUserId());
        assertEquals("CANCELLED", event.getStatus());
    }

    @Test
    void cancelOrderShouldThrowWhenOrderNotFound() {

        repository.orderById = Optional.empty();

        OrderNotFoundException ex = assertThrows(
                OrderNotFoundException.class,
                () -> orderService.cancelOrder("missing-id")
        );

        assertEquals(
                "Order not found with id: missing-id",
                ex.getMessage()
        );
    }

    @Test
    void cancelOrderShouldThrowWhenOrderAlreadyCancelled() {

        Order order = new Order("u1", "p1", 2);

        order.setId("order-1");
        order.setStatus("CANCELLED");

        repository.orderById = Optional.of(order);

        OrderAlreadyCancelledException ex = assertThrows(
                OrderAlreadyCancelledException.class,
                () -> orderService.cancelOrder("order-1")
        );

        assertEquals(
                "Order is already cancelled",
                ex.getMessage()
        );
    }

    @Test
    void updateOrderShouldUpdateFields() {

        Order order = new Order("u1", "p1", 2);

        order.setId("order-1");
        order.setStatus("CREATED");

        repository.orderById = Optional.of(order);

        UpdateOrderRequest request = new UpdateOrderRequest();

        request.setQuantity(10);
        request.setProductId("updated-product");

        Optional<Order> updatedOrder =
                orderService.updateOrder("order-1", request);

        assertTrue(updatedOrder.isPresent());

        Order result = updatedOrder.get();

        assertEquals(10, result.getQuantity());
        assertEquals("updated-product", result.getProductId());

        assertInstanceOf(
                OrderUpdatedEvent.class,
                kafkaProducer.sentEvent
        );

        OrderUpdatedEvent event =
                (OrderUpdatedEvent) kafkaProducer.sentEvent;

        assertEquals("order-1", event.getOrderId());
        assertEquals(10, event.getQuantity());
        assertEquals("updated-product", event.getProductId());
    }

    @Test
    void updateOrderShouldThrowWhenOrderCancelled() {

        Order order = new Order("u1", "p1", 2);

        order.setId("order-1");
        order.setStatus("CANCELLED");

        repository.orderById = Optional.of(order);

        UpdateOrderRequest request = new UpdateOrderRequest();

        request.setQuantity(5);

        OrderNotUpdatableException ex = assertThrows(
                OrderNotUpdatableException.class,
                () -> orderService.updateOrder("order-1", request)
        );

        assertEquals(
                "Cannot update a cancelled order",
                ex.getMessage()
        );
    }

    @Test
    void updateOrderShouldThrowWhenQuantityInvalid() {

        Order order = new Order("u1", "p1", 2);

        order.setId("order-1");
        order.setStatus("CREATED");

        repository.orderById = Optional.of(order);

        UpdateOrderRequest request = new UpdateOrderRequest();

        request.setQuantity(0);

        InvalidOrderException ex = assertThrows(
                InvalidOrderException.class,
                () -> orderService.updateOrder("order-1", request)
        );

        assertEquals(
                "Quantity must be greater than zero",
                ex.getMessage()
        );
    }

    @Test
    void updateOrderShouldReturnEmptyWhenOrderNotFound() {

        repository.orderById = Optional.empty();

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setQuantity(5);
        request.setProductId("p1");

        Optional<Order> result = orderService.updateOrder("missing-id", request);

        assertFalse(result.isPresent());
        assertNull(kafkaProducer.sentEvent);
    }

    @Test
    void updateOrderShouldUpdateOnlyQuantityWhenProductIdIsNull() {

        Order order = new Order("u1", "p1", 2);
        order.setId("order-1");
        order.setStatus("CREATED");

        repository.orderById = Optional.of(order);

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setQuantity(10);
        request.setProductId(null);

        Optional<Order> result = orderService.updateOrder("order-1", request);

        assertTrue(result.isPresent());
        Order updated = result.get();

        assertEquals(10, updated.getQuantity());
        assertEquals("p1", updated.getProductId());

        assertInstanceOf(OrderUpdatedEvent.class, kafkaProducer.sentEvent);
    }

    @Test
    void updateOrderShouldUpdateOnlyProductIdWhenQuantityIsNull() {

        Order order = new Order("u1", "p1", 2);
        order.setId("order-1");
        order.setStatus("CREATED");

        repository.orderById = Optional.of(order);

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setQuantity(null);
        request.setProductId("new-product");

        Optional<Order> result = orderService.updateOrder("order-1", request);

        assertTrue(result.isPresent());
        Order updated = result.get();

        assertEquals(2, updated.getQuantity());
        assertEquals("new-product", updated.getProductId());

        assertInstanceOf(OrderUpdatedEvent.class, kafkaProducer.sentEvent);
    }

    private static class FakeOrderRepository {

        private Order savedOrder;
        private List<Order> orders = new ArrayList<>();
        private Optional<Order> orderById = Optional.empty();
        private String requestedId;

        private OrderRepository proxy() {
            return (OrderRepository) Proxy.newProxyInstance(
                    OrderRepository.class.getClassLoader(),
                    new Class<?>[] {OrderRepository.class},
                    (proxy, method, args) -> {
                        String methodName = method.getName();

                        if ("save".equals(methodName)) {
                            Order order = (Order) args[0];
                            order.setId("order-1");
                            savedOrder = order;
                            return order;
                        }

                        if ("findAll".equals(methodName) && method.getParameterCount() == 0) {
                            return orders;
                        }

                        if ("findById".equals(methodName)) {
                            requestedId = (String) args[0];
                            return orderById;
                        }

                        if ("toString".equals(methodName)) {
                            return "FakeOrderRepository";
                        }

                        throw new UnsupportedOperationException(methodName + " is not supported by this test fake");
                    });
        }
    }

    private static class FakeOrderKafkaProducer extends OrderKafkaProducer {

        private Event sentEvent;

        private FakeOrderKafkaProducer() {
            super(null, "order-created", "order-cancelled", "order-updated");
        }

        @Override
        public void sendOrderCreatedEvent(OrderCreatedEvent event) {
            sentEvent = event;
        }

        @Override
        public void sendOrderCancelledEvent(OrderCancelledEvent event) {
            sentEvent = event;
        }

        @Override
        public void sendOrderUpdatedEvent(OrderUpdatedEvent event) {
            sentEvent = event;
        }
    }

    private static class FakeInventoryService extends InventoryService {
        private InventoryItem inventoryItem = null;
        private boolean shouldThrowNotEnough = false;

        private FakeInventoryService() {
            super(null);
        }

        @Override
        public Optional<InventoryItem> reserveStock(InventoryRequest request) {
            if (inventoryItem == null || !inventoryItem.getProductId().equals(request.getProductId())) {
                throw new InventoryNotFoundException("Inventory item not found for product " + request.getProductId());
            }
            if (shouldThrowNotEnough) {
                throw new InventoryNotEnoughException("Insufficient stock for product " + request.getProductId());
            }
            return Optional.of(new InventoryItem());
        }

        @Override
        public Optional<InventoryItem> releaseStock(InventoryRequest request) {
            if (inventoryItem == null || !inventoryItem.getProductId().equals(request.getProductId())) {
                throw new InventoryNotFoundException("Inventory item not found for product " + request.getProductId());
            }
            return Optional.of(new InventoryItem());
        }

        @Override
        public Optional<InventoryItem> getInventory(String productId) {
            if (inventoryItem == null || !inventoryItem.getProductId().equals(productId)) {
                throw new InventoryNotFoundException("Inventory item not found for product " + productId);
            }
            return Optional.of(inventoryItem);
        }
    }
}
