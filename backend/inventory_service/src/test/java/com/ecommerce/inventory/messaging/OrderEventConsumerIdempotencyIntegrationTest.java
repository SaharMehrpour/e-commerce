package com.ecommerce.inventory.messaging;

import com.ecommerce.inventory.idempotency.ProcessedEventRepository;
import com.ecommerce.inventory.service.InventoryService;
import com.ecommerce.shared.domain.order.OrderStatus;
import com.ecommerce.shared.dto.inventory.InventoryRequest;
import com.ecommerce.shared.event.order.OrderCancelledEvent;
import com.ecommerce.shared.event.order.OrderCreatedEvent;
import com.ecommerce.shared.event.order.OrderUpdatedEvent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class OrderEventConsumerIdempotencyIntegrationTest {

    @Autowired
    private OrderEventConsumer orderEventConsumer;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @MockitoBean
    private InventoryService inventoryService;

    @AfterEach
    void tearDown() {
        processedEventRepository.deleteAll();
        reset(inventoryService);
    }

    @Test
    void shouldProcessEventOnlyOnce_whenDuplicateEventArrives() {

        OrderCreatedEvent event = new OrderCreatedEvent("order-1", "user-1", "product-1", 2, OrderStatus.CREATED);

        ReflectionTestUtils.setField(event, "eventId", "event-123");

        orderEventConsumer.handleOrderCreated(event);
        orderEventConsumer.handleOrderCreated(event);

        verify(inventoryService, times(1))
                .reserveStock(any(InventoryRequest.class));

        assertEquals(1, processedEventRepository.count());
    }

    @Test
    void shouldProcessCancelEventOnlyOnce_whenDuplicateEventArrives() {

        OrderCancelledEvent event = new OrderCancelledEvent("order-1", "user-1", "product-1", 3, OrderStatus.CANCELLED);

        ReflectionTestUtils.setField(event, "eventId", "event-456");

        orderEventConsumer.handleOrderCancelled(event);
        orderEventConsumer.handleOrderCancelled(event);

        verify(inventoryService, times(1))
                .releaseStock(any(InventoryRequest.class));

        assertEquals(1, processedEventRepository.count());
    }

    @Test
    void shouldProcessUpdateEventOnlyOnce_whenDuplicateEventArrives() {

        OrderUpdatedEvent event = new OrderUpdatedEvent("order-1", "product-1", 2, "product-1", 5);

        ReflectionTestUtils.setField(event, "eventId", "event-789");

        orderEventConsumer.handleOrderUpdated(event);
        orderEventConsumer.handleOrderUpdated(event);

        verify(inventoryService, times(1)).reserveStock(any());
        verify(inventoryService, never()).releaseStock(any());

        assertEquals(1, processedEventRepository.count());
    }
}