package com.ecommerce.inventory.messaging;

import com.ecommerce.inventory.idempotency.ProcessedEvent;
import com.ecommerce.inventory.idempotency.ProcessedEventRepository;
import com.ecommerce.inventory.service.InventoryService;
import com.ecommerce.shared.dto.inventory.InventoryRequest;
import com.ecommerce.shared.event.order.OrderCancelledEvent;
import com.ecommerce.shared.event.order.OrderCreatedEvent;
import com.ecommerce.shared.event.order.OrderUpdatedEvent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

                OrderCreatedEvent event = new OrderCreatedEvent("o1", "u1", "p1", 5, null);

                ReflectionTestUtils.setField(event, "eventId", "event-1");

                when(processedEventRepository.existsById("event-1"))
                                .thenReturn(false);

                orderEventConsumer.handleOrderCreated(event);

                ArgumentCaptor<InventoryRequest> captor = ArgumentCaptor.forClass(InventoryRequest.class);

                verify(inventoryService, times(1))
                                .reserveStock(captor.capture());

                InventoryRequest request = captor.getValue();

                assertEquals("p1", request.getProductId());
                assertEquals(5, request.getQuantity());

                verify(processedEventRepository, times(1))
                                .save(any(ProcessedEvent.class));
        }

        @Test
        void handleOrderCreatedShouldSkipIfAlreadyProcessed() {

                OrderCreatedEvent event = new OrderCreatedEvent("o1", "u1", "p1", 5, null);

                ReflectionTestUtils.setField(event, "eventId", "event-1");

                when(processedEventRepository.existsById("event-1"))
                                .thenReturn(true);

                orderEventConsumer.handleOrderCreated(event);

                verify(inventoryService, never()).reserveStock(any());
                verify(processedEventRepository, never()).save(any());
        }

        @Test
        void handleOrderCancelledShouldReleaseStock() {

                OrderCancelledEvent event = new OrderCancelledEvent("o1", "u1", "p1", 3, null);

                ReflectionTestUtils.setField(event, "eventId", "event-1");

                when(processedEventRepository.existsById("event-1"))
                                .thenReturn(false);

                orderEventConsumer.handleOrderCancelled(event);

                ArgumentCaptor<InventoryRequest> captor = ArgumentCaptor.forClass(InventoryRequest.class);

                verify(inventoryService, times(1))
                                .releaseStock(captor.capture());

                InventoryRequest request = captor.getValue();

                assertEquals("p1", request.getProductId());
                assertEquals(3, request.getQuantity());

                verify(processedEventRepository, times(1))
                                .save(any(ProcessedEvent.class));
        }

        @Test
        void handleOrderCancelledShouldSkipIfAlreadyProcessed() {

                OrderCancelledEvent event = new OrderCancelledEvent("o1", "u1", "p1", 3, null);

                ReflectionTestUtils.setField(event, "eventId", "event-1");

                when(processedEventRepository.existsById("event-1"))
                                .thenReturn(true);

                orderEventConsumer.handleOrderCancelled(event);

                verify(inventoryService, never()).releaseStock(any());
                verify(processedEventRepository, never()).save(any());
        }

        @Test
        void handleOrderUpdatedShouldReleaseAndReserveWhenProductChanges() {

                OrderUpdatedEvent event = new OrderUpdatedEvent("o1", "old-p1", 2, "new-p2", 4);

                ReflectionTestUtils.setField(event, "eventId", "event-1");

                when(processedEventRepository.existsById("event-1"))
                                .thenReturn(false);

                orderEventConsumer.handleOrderUpdated(event);

                ArgumentCaptor<InventoryRequest> releaseCaptor = ArgumentCaptor.forClass(InventoryRequest.class);

                ArgumentCaptor<InventoryRequest> reserveCaptor = ArgumentCaptor.forClass(InventoryRequest.class);

                verify(inventoryService, times(1))
                                .releaseStock(releaseCaptor.capture());

                verify(inventoryService, times(1))
                                .reserveStock(reserveCaptor.capture());

                assertEquals("old-p1", releaseCaptor.getValue().getProductId());
                assertEquals("new-p2", reserveCaptor.getValue().getProductId());

                verify(processedEventRepository, times(1))
                                .save(any(ProcessedEvent.class));
        }

        @Test
        void handleOrderUpdatedShouldOnlyReserveWhenSameProduct() {

                OrderUpdatedEvent event = new OrderUpdatedEvent("o1", "p1", 2, "p1", 4);

                ReflectionTestUtils.setField(event, "eventId", "event-1");

                when(processedEventRepository.existsById("event-1"))
                                .thenReturn(false);

                orderEventConsumer.handleOrderUpdated(event);

                verify(inventoryService, never()).releaseStock(any());
                verify(inventoryService, times(1)).reserveStock(any());

                verify(processedEventRepository, times(1))
                                .save(any(ProcessedEvent.class));
        }

        @Test
        void handleOrderUpdatedShouldReleaseDeltaWhenQuantityDecreases() {

                OrderUpdatedEvent event = new OrderUpdatedEvent("o1", "p1", 5, "p1", 2);

                ReflectionTestUtils.setField(event, "eventId", "event-1");

                when(processedEventRepository.existsById("event-1"))
                                .thenReturn(false);

                orderEventConsumer.handleOrderUpdated(event);

                ArgumentCaptor<InventoryRequest> captor = ArgumentCaptor.forClass(InventoryRequest.class);

                verify(inventoryService, times(1))
                                .releaseStock(captor.capture());

                assertEquals(3, captor.getValue().getQuantity());

                verify(processedEventRepository, times(1))
                                .save(any(ProcessedEvent.class));
        }

        @Test
        void handleOrderUpdatedShouldDoNothingWhenNoChange() {

                OrderUpdatedEvent event = new OrderUpdatedEvent("o1", "p1", 5, "p1", 5);

                ReflectionTestUtils.setField(event, "eventId", "event-1");

                when(processedEventRepository.existsById("event-1"))
                                .thenReturn(false);

                orderEventConsumer.handleOrderUpdated(event);

                verify(inventoryService, never()).reserveStock(any());
                verify(inventoryService, never()).releaseStock(any());

                verify(processedEventRepository, times(1))
                                .save(any(ProcessedEvent.class));
        }
}