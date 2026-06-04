package com.ecommerce.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.InventoryResponse;
import com.ecommerce.dto.UpdateOrderRequest;
import com.ecommerce.event.*;
import com.ecommerce.exception.InvalidOrderException;
import com.ecommerce.exception.InventoryNotEnoughException;
import com.ecommerce.exception.InventoryNotFoundException;
import com.ecommerce.exception.OrderAlreadyCancelledException;
import com.ecommerce.exception.OrderNotFoundException;
import com.ecommerce.exception.OrderNotUpdatableException;
import com.ecommerce.kafka.producer.OrderEventProducer;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository repository;

    @Mock
    private OrderEventProducer kafkaProducer;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OrderService orderService;

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
        mockSavedOrder.setStatus("CREATED");

        when(restTemplate.getForObject(anyString(), eq(InventoryResponse.class)))
                .thenReturn(mockInventory);
        when(repository.save(any(Order.class)))
                .thenReturn(mockSavedOrder);

        Order createdOrder = orderService.createOrder(request);

        assertNotNull(createdOrder);
        assertEquals("order-1", createdOrder.getId());
        assertEquals("CREATED", createdOrder.getStatus());

        verify(repository, times(1)).save(any(Order.class));

        ArgumentCaptor<OrderCreatedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(kafkaProducer, times(1)).sendOrderCreatedEvent(eventCaptor.capture());

        OrderCreatedEvent sentEvent = eventCaptor.getValue();
        assertNotNull(sentEvent);
        assertEquals("order-1", sentEvent.getOrderId());
        assertEquals("u1", sentEvent.getUserId());
        assertEquals("p1", sentEvent.getProductId());
        assertEquals(2, sentEvent.getQuantity());
        assertEquals("CREATED", sentEvent.getStatus());
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
        verifyNoInteractions(repository);
        verifyNoInteractions(kafkaProducer);
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
        verifyNoInteractions(repository);
        verifyNoInteractions(kafkaProducer);
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
        verifyNoInteractions(repository);
        verifyNoInteractions(kafkaProducer);
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
        verifyNoInteractions(repository);
        verifyNoInteractions(kafkaProducer);
    }

    @Test
    void createOrderShouldThrowInventoryNotFoundExceptionWhenInventoryServiceReturnsEmpty() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId("u1");
        request.setProductId("p1");
        request.setQuantity(2);

        when(restTemplate.getForObject(anyString(), eq(InventoryResponse.class)))
                .thenReturn(null);

        InventoryNotFoundException ex = assertThrows(
                InventoryNotFoundException.class,
                () -> orderService.createOrder(request)
        );

        assertEquals("Inventory item not found for product p1", ex.getMessage());
        verify(repository, never()).save(any());
        verifyNoInteractions(kafkaProducer);
    }

    @Test
    void createOrderShouldThrowWhenNotEnoughStock() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId("u1");
        request.setProductId("p1");
        request.setQuantity(5); // Requesting 5 items

        InventoryResponse mockInventory = new InventoryResponse();
        mockInventory.setProductId("p1");
        mockInventory.setAvailableQuantity(2);

        when(restTemplate.getForObject(anyString(), eq(InventoryResponse.class)))
                .thenReturn(mockInventory);

        InventoryNotEnoughException ex = assertThrows(
                InventoryNotEnoughException.class,
                () -> orderService.createOrder(request));

        assertEquals("Insufficient stock for product p1", ex.getMessage());

        verifyNoInteractions(repository);
        verifyNoInteractions(kafkaProducer);
    }

    @Test
    void getOrderShouldReturnRepositoryOrderById() {
        Order order = new Order("u1", "p1", 2);
        when(repository.findById("order-1")).thenReturn(Optional.of(order));
        Optional<Order> foundOrder = orderService.getOrder("order-1");

        assertTrue(foundOrder.isPresent());
        assertSame(order, foundOrder.get());
        verify(repository, times(1)).findById("order-1");
    }

    @Test
    void getOrderShouldReturnNotPresentForNonExistentOrder() {
        when(repository.findById("non-existent-id")).thenReturn(Optional.empty());
        Optional<Order> foundOrder = orderService.getOrder("non-existent-id");

        assertFalse(foundOrder.isPresent());
        verify(repository, times(1)).findById("non-existent-id");
    }

    @Test
    void getOrdersShouldReturnRepositoryOrders() {
        Order firstOrder = new Order("u1", "p1", 2);
        Order secondOrder = new Order("u2", "p2", 4);
        when(repository.findAll()).thenReturn(List.of(firstOrder, secondOrder));

        List<Order> orders = orderService.getOrders();

        assertEquals(List.of(firstOrder, secondOrder), orders);
        verify(repository, times(1)).findAll();
    }

    @Test
    void cancelOrderShouldPersistCancelledStatus() {
        Order order = new Order("u1", "p1", 2);
        order.setId("order-1");
        order.setStatus("CREATED");

        when(repository.findById("order-1")).thenReturn(Optional.of(order));
        when(repository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Order> cancelledOrder = orderService.cancelOrder("order-1");

        assertTrue(cancelledOrder.isPresent());
        Order result = cancelledOrder.get();
        assertEquals("CANCELLED", result.getStatus());

        verify(repository, times(1)).save(order);

        ArgumentCaptor<OrderCancelledEvent> eventCaptor = ArgumentCaptor.forClass(OrderCancelledEvent.class);
        verify(kafkaProducer, times(1)).sendOrderCancelledEvent(eventCaptor.capture());

        OrderCancelledEvent event = eventCaptor.getValue();
        assertEquals("order-1", event.getOrderId());
        assertEquals("u1", event.getUserId());
        assertEquals("p1", event.getProductId());
        assertEquals(2, event.getQuantity());
        assertEquals("CANCELLED", event.getStatus());
    }

    @Test
    void cancelOrderShouldThrowWhenOrderNotFound() {
        when(repository.findById("missing-id")).thenReturn(Optional.empty());

        OrderNotFoundException ex = assertThrows(
                OrderNotFoundException.class,
                () -> orderService.cancelOrder("missing-id")
        );

        assertEquals("Order not found with id: missing-id", ex.getMessage());
        verify(repository, never()).save(any());
        verifyNoInteractions(kafkaProducer);
    }

    @Test
    void cancelOrderShouldThrowWhenOrderAlreadyCancelled() {
        Order order = new Order("u1", "p1", 2);
        order.setId("order-1");
        order.setStatus("CANCELLED");

        when(repository.findById("order-1")).thenReturn(Optional.of(order));

        OrderAlreadyCancelledException ex = assertThrows(
                OrderAlreadyCancelledException.class,
                () -> orderService.cancelOrder("order-1")
        );

        assertEquals("Order is already cancelled", ex.getMessage());
        verify(repository, never()).save(any());
        verifyNoInteractions(kafkaProducer);
    }

    @Test
    void updateOrderShouldUpdateFields() {
        Order order = new Order("u1", "p1", 2);
        order.setId("order-1");
        order.setStatus("CREATED");

        when(repository.findById("order-1")).thenReturn(Optional.of(order));
        when(repository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setQuantity(10);
        request.setProductId("updated-product");

        Optional<Order> updatedOrder = orderService.updateOrder("order-1", request);

        assertTrue(updatedOrder.isPresent());
        Order result = updatedOrder.get();
        assertEquals(10, result.getQuantity());
        assertEquals("updated-product", result.getProductId());

        ArgumentCaptor<OrderUpdatedEvent> eventCaptor = ArgumentCaptor.forClass(OrderUpdatedEvent.class);
        verify(kafkaProducer, times(1)).sendOrderUpdatedEvent(eventCaptor.capture());

        OrderUpdatedEvent event = eventCaptor.getValue();
        assertEquals("order-1", event.getOrderId());
        assertEquals("p1", event.getOldProductId());
        assertEquals(2, event.getOldQuantity());
        assertEquals("updated-product", event.getNewProductId());
        assertEquals(10, event.getNewQuantity());
    }

    @Test
    void updateOrderShouldThrowWhenOrderCancelled() {
        Order order = new Order("u1", "p1", 2);
        order.setId("order-1");
        order.setStatus("CANCELLED");

        when(repository.findById("order-1")).thenReturn(Optional.of(order));

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setQuantity(5);

        OrderNotUpdatableException ex = assertThrows(
                OrderNotUpdatableException.class,
                () -> orderService.updateOrder("order-1", request)
        );

        assertEquals("Cannot update a cancelled order", ex.getMessage());
        verify(repository, never()).save(any());
        verifyNoInteractions(kafkaProducer);
    }

    @Test
    void updateOrderShouldThrowWhenQuantityInvalid() {
        Order order = new Order("u1", "p1", 2);
        order.setId("order-1");
        order.setStatus("CREATED");

        when(repository.findById("order-1")).thenReturn(Optional.of(order));

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setQuantity(0);

        InvalidOrderException ex = assertThrows(
                InvalidOrderException.class,
                () -> orderService.updateOrder("order-1", request)
        );

        assertEquals("Quantity must be greater than zero", ex.getMessage());
        verify(repository, never()).save(any());
        verifyNoInteractions(kafkaProducer);
    }

    @Test
    void updateOrderShouldThrowNotFoundExceptionWhenOrderNotFound() {
        when(repository.findById("missing-id")).thenReturn(Optional.empty());

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setQuantity(5);
        request.setProductId("p1");

        OrderNotFoundException ex = assertThrows(
                OrderNotFoundException.class,
                () -> orderService.updateOrder("missing-id", request)
        );

        assertEquals("Order not found with id: missing-id", ex.getMessage());
        verify(repository, never()).save(any());
        verifyNoInteractions(kafkaProducer);
    }

    @Test
    void updateOrderShouldNotThrowWhenNeitherFieldProvided() {
        Order order = new Order("u1", "p1", 2);
        order.setId("order-1");
        order.setStatus("CREATED");

        when(repository.findById("order-1")).thenReturn(Optional.of(order));
        when(repository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setQuantity(null);
        request.setProductId(null);

        Optional<Order> result = orderService.updateOrder("order-1", request);

        assertTrue(result.isPresent());
        Order updated = result.get();
        assertEquals(2, updated.getQuantity());
        assertEquals("p1", updated.getProductId());
        verify(repository, times(1)).save(order);
        verify(kafkaProducer, times(1)).sendOrderUpdatedEvent(any());
    }

    @Test
    void updateOrderShouldUpdateOnlyQuantityWhenProductIdIsNull() {
        Order order = new Order("u1", "p1", 2);
        order.setId("order-1");
        order.setStatus("CREATED");

        when(repository.findById("order-1")).thenReturn(Optional.of(order));
        when(repository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setQuantity(10);
        request.setProductId(null);

        Optional<Order> result = orderService.updateOrder("order-1", request);

        assertTrue(result.isPresent());
        Order updated = result.get();
        assertEquals(10, updated.getQuantity());
        assertEquals("p1", updated.getProductId());
        verify(kafkaProducer, times(1)).sendOrderUpdatedEvent(any());
    }

    @Test
    void updateOrderShouldUpdateOnlyProductIdWhenQuantityIsNull() {
        Order order = new Order("u1", "p1", 2);
        order.setId("order-1");
        order.setStatus("CREATED");

        when(repository.findById("order-1")).thenReturn(Optional.of(order));
        when(repository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setQuantity(null);
        request.setProductId("new-product");

        Optional<Order> result = orderService.updateOrder("order-1", request);

        assertTrue(result.isPresent());
        Order updated = result.get();
        assertEquals(2, updated.getQuantity());
        assertEquals("new-product", updated.getProductId());
        verify(kafkaProducer, times(1)).sendOrderUpdatedEvent(any());
    }

    @Test
    void getInventoryShouldReturnInventoryResponseFromRestTemplate() {
        InventoryResponse expectedResponse = new InventoryResponse();
        expectedResponse.setProductId("p1");
        expectedResponse.setAvailableQuantity(10);

        when(restTemplate.getForObject(anyString(), eq(InventoryResponse.class)))
                .thenReturn(expectedResponse);

        Optional<InventoryResponse> actualResponse = orderService.getInventory("p1");

        assertTrue(actualResponse.isPresent());
        assertEquals(expectedResponse, actualResponse.get());
        verify(restTemplate, times(1)).getForObject(anyString(), eq(InventoryResponse.class));
    }

    @Test
    void getInventoryShouldThrowNotFoundWhenRestTemplateThrows() {
        HttpClientErrorException notFoundException = HttpClientErrorException.create(
                org.springframework.http.HttpStatus.NOT_FOUND, "Not Found", null, null, null
        );

        when(restTemplate.getForObject(anyString(), eq(InventoryResponse.class)))
                .thenThrow(notFoundException);

        InventoryNotFoundException ex = assertThrows(
                InventoryNotFoundException.class,
                () -> orderService.getInventory("p1")
        );

        assertEquals("Inventory item not found for product p1", ex.getMessage());
        verify(restTemplate, times(1)).getForObject(anyString(), eq(InventoryResponse.class));
    }
}
