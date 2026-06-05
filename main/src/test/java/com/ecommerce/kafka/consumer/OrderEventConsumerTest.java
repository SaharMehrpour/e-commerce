package com.ecommerce.kafka.consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.dto.InventoryRequest;
import com.ecommerce.event.OrderCancelledEvent;
import com.ecommerce.event.OrderCreatedEvent;
import com.ecommerce.event.OrderUpdatedEvent;
import com.ecommerce.inventory.InventoryService;
import com.ecommerce.kafka.idempotency.ProcessedEvent;
import com.ecommerce.kafka.idempotency.ProcessedEventRepository;

@ExtendWith(MockitoExtension.class)
class OrderEventConsumerTest {

    @Mock
    private InventoryService inventoryService;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @InjectMocks
    private OrderEventConsumer orderEventConsumer;

    @Test
    void handleOrderCreatedShouldReserveStock() {
        OrderCreatedEvent event = new OrderCreatedEvent(
            "event-1", "o1", "u1", "p1", 5, "CREATED"
        );

        when(processedEventRepository.existsById("event-1")).thenReturn(false);

        orderEventConsumer.handleOrderCreated(event);

        ArgumentCaptor<InventoryRequest> captor = ArgumentCaptor.forClass(InventoryRequest.class);

        verify(inventoryService, times(1)).reserveStock(captor.capture());

        InventoryRequest capturedRequest = captor.getValue();
        assertEquals("p1", capturedRequest.getProductId());
        assertEquals(5, capturedRequest.getQuantity());

        verify(processedEventRepository, times(1)).save(any(ProcessedEvent.class));
    }

    @Test
    void handleOrderCreatedShouldSkipIfAlreadyProcessed() {
        OrderCreatedEvent event = new OrderCreatedEvent(
                "event-1", "o1", "u1", "p1", 5, "CREATED"
        );

        when(processedEventRepository.existsById("event-1")).thenReturn(true);

        orderEventConsumer.handleOrderCreated(event);

        verify(inventoryService, never()).reserveStock(any());
        verify(processedEventRepository, never()).save(any());
    }

    @Test
    void handleOrderCancelledShouldReleaseStock() {
        OrderCancelledEvent event = new OrderCancelledEvent(
            "event-1", "o1", "u1", "p1", 3, "CANCELLED"
        );

        when(processedEventRepository.existsById("event-1")).thenReturn(false);

        orderEventConsumer.handleOrderCancelled(event);

        ArgumentCaptor<InventoryRequest> captor =
                ArgumentCaptor.forClass(InventoryRequest.class);

        verify(inventoryService, times(1)).releaseStock(captor.capture());

        InventoryRequest capturedRequest = captor.getValue();
        assertEquals("p1", capturedRequest.getProductId());
        assertEquals(3, capturedRequest.getQuantity());

        verify(processedEventRepository, times(1)).save(any(ProcessedEvent.class));
    }

    @Test
    void handleOrderCancelledShouldSkipIfAlreadyProcessed() {
        OrderCancelledEvent event = new OrderCancelledEvent(
            "event-1", "o1", "u1", "p1", 3, "CANCELLED"
        );

        when(processedEventRepository.existsById("event-1")).thenReturn(true);

        orderEventConsumer.handleOrderCancelled(event);

        verify(inventoryService, never()).releaseStock(any());
        verify(processedEventRepository, never()).save(any());
    }

    @Test
    void handleOrderUpdatedShouldSkipIfAlreadyProcessed() {
        OrderUpdatedEvent event = new OrderUpdatedEvent(
                "event-1",
                "o1",
                "p1",
                5,
                "p1",
                5
        );

        when(processedEventRepository.existsById("event-1")).thenReturn(true);

        orderEventConsumer.handleOrderUpdated(event);

        verify(inventoryService, never()).reserveStock(any());
        verify(processedEventRepository, never()).save(any());
    }

    @Test
    void handleOrderUpdatedShouldReleaseOldAndReserveNewWhenProductIdsDiffer() {
        OrderUpdatedEvent event = new OrderUpdatedEvent(
                "event-1",
                "o1",
                "old-p1",
                2,
                "new-p2",
                4
        );

        when(processedEventRepository.existsById("event-1")).thenReturn(false);

        orderEventConsumer.handleOrderUpdated(event);

        ArgumentCaptor<InventoryRequest> releaseCaptor =
                ArgumentCaptor.forClass(InventoryRequest.class);

        verify(inventoryService, times(1)).releaseStock(releaseCaptor.capture());
        InventoryRequest capturedRelease = releaseCaptor.getValue();
        assertEquals("old-p1", capturedRelease.getProductId());
        assertEquals(2, capturedRelease.getQuantity());

        ArgumentCaptor<InventoryRequest> reserveCaptor =
                ArgumentCaptor.forClass(InventoryRequest.class);

        verify(inventoryService, times(1)).reserveStock(reserveCaptor.capture());
        InventoryRequest capturedReserve = reserveCaptor.getValue();
        assertEquals("new-p2", capturedReserve.getProductId());
        assertEquals(4, capturedReserve.getQuantity());

        verify(processedEventRepository, times(1)).save(any(ProcessedEvent.class));
    }

    @Test
    void handleOrderUpdatedShouldOnlyReserveNewWhenProductIdsAreSame() {
        OrderUpdatedEvent event = new OrderUpdatedEvent(
                "event-1",
                "o1",
                "p1",
                2,
                "p1",
                4
        );

        when(processedEventRepository.existsById("event-1")).thenReturn(false);

        orderEventConsumer.handleOrderUpdated(event);

        verify(inventoryService, never()).releaseStock(any());

        ArgumentCaptor<InventoryRequest> reserveCaptor =
                ArgumentCaptor.forClass(InventoryRequest.class);

        verify(inventoryService, times(1)).reserveStock(reserveCaptor.capture());
        InventoryRequest capturedReserve = reserveCaptor.getValue();
        assertEquals("p1", capturedReserve.getProductId());
        assertEquals(2, capturedReserve.getQuantity());

        verify(processedEventRepository, times(1)).save(any(ProcessedEvent.class));
    }

    @Test
    void handleOrderUpdatedShouldReleaseDeltaWhenSameProductQuantityDecreases() {
        OrderUpdatedEvent event = new OrderUpdatedEvent(
                "event-1",
                "o1",
                "p1",
                5,
                "p1",
                2
        );

        when(processedEventRepository.existsById("event-1")).thenReturn(false);

        orderEventConsumer.handleOrderUpdated(event);

        verify(inventoryService, never()).reserveStock(any());

        ArgumentCaptor<InventoryRequest> releaseCaptor =
                ArgumentCaptor.forClass(InventoryRequest.class);

        verify(inventoryService, times(1)).releaseStock(releaseCaptor.capture());
        InventoryRequest capturedRelease = releaseCaptor.getValue();
        assertEquals("p1", capturedRelease.getProductId());
        assertEquals(3, capturedRelease.getQuantity());

        verify(processedEventRepository, times(1)).save(any(ProcessedEvent.class));
    }

    @Test
    void handleOrderUpdatedShouldDoNothingWhenSameProductQuantityUnchanged() {
        OrderUpdatedEvent event = new OrderUpdatedEvent(
                "event-1",
                "o1",
                "p1",
                5,
                "p1",
                5
        );

        when(processedEventRepository.existsById("event-1")).thenReturn(false);

        orderEventConsumer.handleOrderUpdated(event);

        verify(inventoryService, never()).reserveStock(any());
        verify(inventoryService, never()).releaseStock(any());

        verify(processedEventRepository, times(1))
                .save(any(ProcessedEvent.class));
    }
}
