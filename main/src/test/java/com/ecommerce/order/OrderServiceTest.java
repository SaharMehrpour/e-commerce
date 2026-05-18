package com.ecommerce.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ecommerce.event.*;
import com.ecommerce.kafka.OrderKafkaProducer;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    private FakeOrderRepository repository;
    private FakeOrderKafkaProducer kafkaProducer;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        repository = new FakeOrderRepository();
        kafkaProducer = new FakeOrderKafkaProducer();
        orderService = new OrderService(repository.proxy(), kafkaProducer);
    }

    @Test
    void createOrderShouldSaveOrderAndPublishEvent() {
        Order order = new Order("u1", "p1", 2);

        Order createdOrder = orderService.createOrder(order);

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
    void getOrdersShouldReturnRepositoryOrders() {
        Order firstOrder = new Order("u1", "p1", 2);
        Order secondOrder = new Order("u2", "p2", 4);
        repository.orders = List.of(firstOrder, secondOrder);

        List<Order> orders = orderService.getOrders();

        assertEquals(List.of(firstOrder, secondOrder), orders);
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
            super(null, "order-created");
        }

        @Override
        public void sendOrderCreatedEvent(OrderCreatedEvent event) {
            sentEvent = event;
        }
    }
}
