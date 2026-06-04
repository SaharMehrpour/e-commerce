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

@ExtendWith(MockitoExtension.class)
class OrderEventConsumerTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private OrderEventConsumer orderEventConsumer;

    @Test
    void handleOrderCreatedShouldReserveStock() {
        OrderCreatedEvent event = new OrderCreatedEvent("event-1", "o1", "u1", "p1", 5, "CREATED");

        orderEventConsumer.handleOrderCreated(event);

        ArgumentCaptor<InventoryRequest> captor = ArgumentCaptor.forClass(InventoryRequest.class);
        verify(inventoryService, times(1)).reserveStock(captor.capture());

        InventoryRequest capturedRequest = captor.getValue();
        assertEquals("p1", capturedRequest.getProductId());
        assertEquals(5, capturedRequest.getQuantity());
    }

    @Test
    void handleOrderCancelledShouldReleaseStock() {
        OrderCancelledEvent event = new OrderCancelledEvent("event-1", "o1", "u1", "p1", 3, "CANCELLED");

        orderEventConsumer.handleOrderCancelled(event);

        ArgumentCaptor<InventoryRequest> captor = ArgumentCaptor.forClass(InventoryRequest.class);
        verify(inventoryService, times(1)).releaseStock(captor.capture());

        InventoryRequest capturedRequest = captor.getValue();
        assertEquals("p1", capturedRequest.getProductId());
        assertEquals(3, capturedRequest.getQuantity());
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

        orderEventConsumer.handleOrderUpdated(event);

        ArgumentCaptor<InventoryRequest> releaseCaptor = ArgumentCaptor.forClass(InventoryRequest.class);
        verify(inventoryService, times(1)).releaseStock(releaseCaptor.capture());
        InventoryRequest capturedRelease = releaseCaptor.getValue();
        assertEquals("old-p1", capturedRelease.getProductId());
        assertEquals(2, capturedRelease.getQuantity());

        ArgumentCaptor<InventoryRequest> reserveCaptor = ArgumentCaptor.forClass(InventoryRequest.class);
        verify(inventoryService, times(1)).reserveStock(reserveCaptor.capture());
        InventoryRequest capturedReserve = reserveCaptor.getValue();
        assertEquals("new-p2", capturedReserve.getProductId());
        assertEquals(4, capturedReserve.getQuantity());
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

        orderEventConsumer.handleOrderUpdated(event);

        verify(inventoryService, never()).releaseStock(any());

        ArgumentCaptor<InventoryRequest> reserveCaptor = ArgumentCaptor.forClass(InventoryRequest.class);
        verify(inventoryService, times(1)).reserveStock(reserveCaptor.capture());
        InventoryRequest capturedReserve = reserveCaptor.getValue();
        assertEquals("p1", capturedReserve.getProductId());
        assertEquals(2, capturedReserve.getQuantity());
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

        orderEventConsumer.handleOrderUpdated(event);

        verify(inventoryService, never()).reserveStock(any());

        ArgumentCaptor<InventoryRequest> releaseCaptor = ArgumentCaptor.forClass(InventoryRequest.class);
        verify(inventoryService, times(1)).releaseStock(releaseCaptor.capture());
        InventoryRequest capturedRelease = releaseCaptor.getValue();
        assertEquals("p1", capturedRelease.getProductId());
        assertEquals(3, capturedRelease.getQuantity());
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

        orderEventConsumer.handleOrderUpdated(event);

        verify(inventoryService, never()).reserveStock(any());
        verify(inventoryService, never()).releaseStock(any());
    }
}
