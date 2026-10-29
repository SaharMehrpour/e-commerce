package com.ecommerce.order.service;

import com.ecommerce.order.domain.Order;
import com.ecommerce.order.messaging.OrderEventProducer;
import com.ecommerce.order.messaging.ProductValidationClient;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.shared.domain.order.OrderStatus;
import com.ecommerce.shared.dto.order.CreateOrderRequest;
import com.ecommerce.shared.event.order.OrderCancelledEvent;
import com.ecommerce.shared.event.order.OrderCreatedEvent;
import com.ecommerce.shared.event.order.OrderUpdatedEvent;
import com.ecommerce.shared.exception.auth.NotAuthorizedException;
import com.ecommerce.shared.exception.inventory.InvalidOrderException;
import com.ecommerce.shared.exception.order.OrderNotFoundException;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import static org.mockito.Mockito.inOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final String USER_ID = "u1";
    private static final String ORDER_ID = "order-1";

    @Mock
    private OrderRepository repository;

    @Mock
    private OrderEventProducer kafkaProducer;

    @Mock
    private OrderCacheService orderCacheService;

    @Mock
    private ProductValidationClient productValidationClient;

    private MeterRegistry meterRegistry;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();

        orderService = new OrderService(
                repository,
                kafkaProducer,
                orderCacheService,
                productValidationClient,
                meterRegistry);
    }

    private JwtAuthenticationToken customerAuthentication() {
        return authentication(USER_ID, "ROLE_CUSTOMER");
    }

    private JwtAuthenticationToken adminAuthentication() {
        return authentication("admin-1", "ROLE_ADMIN");
    }

    private JwtAuthenticationToken authentication(String userId, String role) {
        Instant now = Instant.now();

        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject(userId)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .build();

        return new JwtAuthenticationToken(
                jwt,
                List.of(new SimpleGrantedAuthority(role)));
    }

    @Test
    void createOrderShouldValidateProductThenSaveOrderAndPublishEvent() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setProductId("p1");
        request.setQuantity(2);

        when(productValidationClient.validateProduct("p1"))
                .thenReturn(true);

        when(repository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(ORDER_ID);
            return order;
        });

        Order createdOrder = orderService.createOrder(
                request,
                customerAuthentication());

        assertNotNull(createdOrder);
        assertEquals(ORDER_ID, createdOrder.getId());
        assertEquals(USER_ID, createdOrder.getUserId());
        assertEquals("p1", createdOrder.getProductId());
        assertEquals(2, createdOrder.getQuantity());
        assertEquals(OrderStatus.PENDING, createdOrder.getStatus());

        InOrder inOrder = inOrder(productValidationClient, repository, kafkaProducer);
        inOrder.verify(productValidationClient).validateProduct("p1");
        inOrder.verify(repository).save(any(Order.class));
        inOrder.verify(kafkaProducer).sendOrderCreatedEvent(any(OrderCreatedEvent.class));

        ArgumentCaptor<OrderCreatedEvent> eventCaptor =
                ArgumentCaptor.forClass(OrderCreatedEvent.class);

        verify(kafkaProducer).sendOrderCreatedEvent(eventCaptor.capture());

        OrderCreatedEvent sentEvent = eventCaptor.getValue();

        assertEquals(ORDER_ID, sentEvent.getOrderId());
        assertEquals(USER_ID, sentEvent.getUserId());
        assertEquals("p1", sentEvent.getProductId());
        assertEquals(2, sentEvent.getQuantity());
        assertEquals(OrderStatus.PENDING, sentEvent.getStatus());
    }

    @Test
    void createOrderShouldThrowWhenProductDoesNotExist() {
            CreateOrderRequest request = new CreateOrderRequest();
            request.setProductId("missing-product");
            request.setQuantity(2);

            when(productValidationClient.validateProduct("missing-product"))
                            .thenReturn(false);

            assertThrows(
                            InvalidOrderException.class,
                            () -> orderService.createOrder(
                                            request,
                                            customerAuthentication()));

            verify(productValidationClient).validateProduct("missing-product");
            verifyNoInteractions(repository, kafkaProducer);
    }

    @Test
    void createOrderShouldThrowWhenRequestIsNull() {
            assertThrows(
                            InvalidOrderException.class,
                            () -> orderService.createOrder(null, customerAuthentication()));

            verifyNoInteractions(
                            repository, kafkaProducer, productValidationClient);
    }

    @Test
    void createOrderShouldThrowWhenProductIdIsMissing() {
            CreateOrderRequest request = new CreateOrderRequest();
            request.setQuantity(2);

            assertThrows(
                            InvalidOrderException.class,
                            () -> orderService.createOrder(request, customerAuthentication()));

            verifyNoInteractions(
                            repository, kafkaProducer, productValidationClient);
    }

    @Test
    void createOrderShouldThrowWhenQuantityIsNotPositive() {
            CreateOrderRequest request = new CreateOrderRequest();
            request.setProductId("p1");
            request.setQuantity(0);

            assertThrows(
                            InvalidOrderException.class,
                            () -> orderService.createOrder(request, customerAuthentication()));

            verifyNoInteractions(
                            repository, kafkaProducer, productValidationClient);
    }

    @Test
    void getOrderShouldReturnOrderForOwner() {
        Order order = new Order();
        order.setId(ORDER_ID);
        order.setUserId(USER_ID);

        when(orderCacheService.getOrder(ORDER_ID))
                .thenReturn(Optional.of(order));

        Optional<Order> foundOrder = orderService.getOrder(
                ORDER_ID,
                customerAuthentication());

        assertTrue(foundOrder.isPresent());
        assertSame(order, foundOrder.get());

        verify(orderCacheService).getOrder(ORDER_ID);
    }

    @Test
    void getOrderShouldReturnEmptyWhenOrderBelongsToAnotherUser() {
        Order order = new Order();
        order.setId(ORDER_ID);
        order.setUserId("another-user");

        when(orderCacheService.getOrder(ORDER_ID))
                .thenReturn(Optional.of(order));

        Optional<Order> foundOrder = orderService.getOrder(
                ORDER_ID,
                customerAuthentication());

        assertTrue(foundOrder.isEmpty());
    }

    @Test
    void getOrderShouldReturnOrderForAdmin() {
        Order order = new Order();
        order.setId(ORDER_ID);
        order.setUserId("another-user");

        when(orderCacheService.getOrder(ORDER_ID))
                .thenReturn(Optional.of(order));

        Optional<Order> foundOrder = orderService.getOrder(
                ORDER_ID,
                adminAuthentication());

        assertTrue(foundOrder.isPresent());
        assertSame(order, foundOrder.get());
    }

    @Test
    void getOrderShouldReturnEmptyWhenOrderDoesNotExist() {
        when(orderCacheService.getOrder(ORDER_ID))
                .thenReturn(Optional.empty());

        Optional<Order> foundOrder = orderService.getOrder(
                ORDER_ID,
                customerAuthentication());

        assertTrue(foundOrder.isEmpty());
    }

    @Test
    void getOrdersShouldReturnOrdersForAuthenticatedUser() {
        Order first = new Order();
        first.setUserId(USER_ID);

        Order second = new Order();
        second.setUserId(USER_ID);

        List<Order> expectedOrders = List.of(first, second);

        when(orderCacheService.getOrdersForUser(USER_ID))
                .thenReturn(expectedOrders);

        List<Order> orders = orderService.getOrders(customerAuthentication());

        assertEquals(2, orders.size());
        assertEquals(expectedOrders, orders);

        verify(orderCacheService).getOrdersForUser(USER_ID);
        verify(orderCacheService, never()).getAllOrders();
    }

    @Test
    void getOrdersShouldReturnAllOrdersForAdmin() {
        Order first = new Order();
        Order second = new Order();

        List<Order> expectedOrders = List.of(first, second);

        when(orderCacheService.getAllOrders())
                .thenReturn(expectedOrders);

        List<Order> orders = orderService.getOrders(adminAuthentication());

        assertEquals(2, orders.size());
        assertEquals(expectedOrders, orders);

        verify(orderCacheService).getAllOrders();
        verify(orderCacheService, never()).getOrdersForUser(anyString());
    }

    @Test
    void cancelOrderShouldPersistCancelledStatus() {
        Order order = new Order();
        order.setId(ORDER_ID);
        order.setUserId(USER_ID);
        order.setProductId("p1");
        order.setQuantity(2);
        order.setStatus(OrderStatus.CREATED);

        when(repository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(repository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Order> result = orderService.cancelOrder(
                ORDER_ID,
                customerAuthentication());

        assertTrue(result.isPresent());
        assertEquals(OrderStatus.CANCELLED, result.get().getStatus());

        verify(repository).save(order);
        verify(kafkaProducer).sendOrderCancelledEvent(
                any(OrderCancelledEvent.class));
    }

    @Test
    void cancelOrderShouldAllowAdminToCancelAnotherUsersOrder() {
        Order order = new Order();
        order.setId(ORDER_ID);
        order.setUserId("another-user");
        order.setProductId("p1");
        order.setQuantity(2);
        order.setStatus(OrderStatus.CREATED);

        when(repository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(repository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Order> result = orderService.cancelOrder(
                ORDER_ID,
                adminAuthentication());

        assertTrue(result.isPresent());
        assertEquals(OrderStatus.CANCELLED, result.get().getStatus());

        verify(repository).save(order);
        verify(kafkaProducer).sendOrderCancelledEvent(
                any(OrderCancelledEvent.class));
    }

    @Test
    void cancelOrderShouldRejectAnotherUsersOrder() {
        Order order = new Order();
        order.setId(ORDER_ID);
        order.setUserId("another-user");
        order.setProductId("p1");
        order.setQuantity(2);
        order.setStatus(OrderStatus.CREATED);

        when(repository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThrows(
                NotAuthorizedException.class,
                () -> orderService.cancelOrder(
                        ORDER_ID,
                        customerAuthentication()));

        verify(repository, never()).save(any(Order.class));
        verifyNoInteractions(kafkaProducer);
    }

    @Test
    void cancelOrderShouldThrowWhenOrderNotFound() {
        when(repository.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.cancelOrder(
                        ORDER_ID,
                        customerAuthentication()));

        verify(repository, never()).save(any(Order.class));
        verifyNoInteractions(kafkaProducer);
    }

    @Test
    void updateOrderShouldUpdateFields() {
        Order order = new Order();
        order.setId(ORDER_ID);
        order.setUserId(USER_ID);
        order.setProductId("p1");
        order.setQuantity(2);
        order.setStatus(OrderStatus.CREATED);

        when(repository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(repository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        com.ecommerce.shared.dto.order.UpdateOrderRequest request = new com.ecommerce.shared.dto.order.UpdateOrderRequest();
        request.setProductId("new-p");
        request.setQuantity(10);

        Optional<Order> updated = orderService.updateOrder(
                ORDER_ID,
                request,
                customerAuthentication());

        assertTrue(updated.isPresent());
        assertEquals("new-p", updated.get().getProductId());
        assertEquals(10, updated.get().getQuantity());

        verify(repository).save(order);
        verify(kafkaProducer).sendOrderUpdatedEvent(
                any(OrderUpdatedEvent.class));
    }

    @Test
    void updateOrderShouldRejectAnotherUsersOrder() {
        Order order = new Order();
        order.setId(ORDER_ID);
        order.setUserId("another-user");
        order.setProductId("p1");
        order.setQuantity(2);
        order.setStatus(OrderStatus.CREATED);

        when(repository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        com.ecommerce.shared.dto.order.UpdateOrderRequest request = new com.ecommerce.shared.dto.order.UpdateOrderRequest();
        request.setQuantity(5);

        assertThrows(
                NotAuthorizedException.class,
                () -> orderService.updateOrder(
                        ORDER_ID,
                        request,
                        customerAuthentication()));

        verify(repository, never()).save(any(Order.class));
        verifyNoInteractions(kafkaProducer);
    }

    @Test
    void updateOrderShouldThrowWhenRequestIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.updateOrder(
                        ORDER_ID,
                        null,
                        customerAuthentication()));

        verifyNoInteractions(repository, kafkaProducer);
    }

    @Test
    void updateOrderShouldThrowWhenOrderNotFound() {
        when(repository.findById(ORDER_ID)).thenReturn(Optional.empty());

        com.ecommerce.shared.dto.order.UpdateOrderRequest request = new com.ecommerce.shared.dto.order.UpdateOrderRequest();
        request.setProductId("new-p");
        request.setQuantity(10);

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.updateOrder(
                        ORDER_ID,
                        request,
                        customerAuthentication()));

        verify(repository, never()).save(any(Order.class));
        verifyNoInteractions(kafkaProducer);
    }
}