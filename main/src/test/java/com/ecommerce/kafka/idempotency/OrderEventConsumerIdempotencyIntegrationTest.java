package com.ecommerce.kafka.idempotency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.ecommerce.dto.InventoryRequest;
import com.ecommerce.event.OrderCancelledEvent;
import com.ecommerce.event.OrderCreatedEvent;
import com.ecommerce.event.OrderUpdatedEvent;
import com.ecommerce.inventory.InventoryService;
import com.ecommerce.kafka.consumer.OrderEventConsumer;

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

        OrderCreatedEvent event = new OrderCreatedEvent(
                "event-123",
                "order-1",
                "user-1",
                "product-1",
                2,
                "CREATED"
        );

        orderEventConsumer.handleOrderCreated(event);
        orderEventConsumer.handleOrderCreated(event);

        verify(inventoryService, times(1))
                .reserveStock(any(InventoryRequest.class));

        assertEquals(1, processedEventRepository.count());
    }

    @Test
    void shouldProcessCancelEventOnlyOnce_whenDuplicateEventArrives() {

        OrderCancelledEvent event = new OrderCancelledEvent(
                "event-456",
                "order-1",
                "user-1",
                "product-1",
                3,
                "CANCELLED"
        );

        orderEventConsumer.handleOrderCancelled(event);
        orderEventConsumer.handleOrderCancelled(event);

        verify(inventoryService, times(1))
                .releaseStock(any(InventoryRequest.class));

        assertEquals(1, processedEventRepository.count());
    }

    @Test
    void shouldProcessUpdateEventOnlyOnce_whenDuplicateEventArrives() {

        OrderUpdatedEvent event = new OrderUpdatedEvent(
                "event-789",
                "order-1",
                "product-1",
                2,
                "product-1",
                5
        );

        orderEventConsumer.handleOrderUpdated(event);
        orderEventConsumer.handleOrderUpdated(event);

        verify(inventoryService, times(1)).reserveStock(any());
        verify(inventoryService, never()).releaseStock(any());

        assertEquals(1, processedEventRepository.count());
    }
}