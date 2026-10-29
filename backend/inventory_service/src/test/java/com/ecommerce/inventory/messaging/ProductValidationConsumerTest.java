package com.ecommerce.inventory.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.ecommerce.inventory.domain.InventoryItem;
import com.ecommerce.inventory.service.InventoryService;
import com.ecommerce.shared.event.inventory.ProductValidationRequest;
import com.ecommerce.shared.event.inventory.ProductValidationResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class ProductValidationConsumerTest {

    private static final String RESPONSE_TOPIC = "product-validation-responses";
    private static final String REQUEST_ID = "request-123";
    private static final String PRODUCT_ID = "product-1";

    @Mock
    private InventoryService inventoryService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private ProductValidationConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new ProductValidationConsumer(
                inventoryService,
                kafkaTemplate,
                RESPONSE_TOPIC);
    }

    @Test
    void handleProductValidationRequest_WhenProductExists_SendsTrueResponse() {
        InventoryItem inventoryItem = org.mockito.Mockito.mock(InventoryItem.class);

        when(inventoryService.getInventory(PRODUCT_ID))
                .thenReturn(Optional.of(inventoryItem));

        ProductValidationRequest request = new ProductValidationRequest(REQUEST_ID, PRODUCT_ID);

        consumer.handleProductValidationRequest(request);

        verify(inventoryService).getInventory(PRODUCT_ID);

        ArgumentCaptor<ProductValidationResponse> responseCaptor = ArgumentCaptor
                .forClass(ProductValidationResponse.class);

        verify(kafkaTemplate).send(
                eq(RESPONSE_TOPIC),
                eq(REQUEST_ID),
                responseCaptor.capture());

        ProductValidationResponse response = responseCaptor.getValue();

        assertEquals(REQUEST_ID, response.getRequestId());
        assertEquals(PRODUCT_ID, response.getProductId());
        assertTrue(response.isExists());
    }

    @Test
    void handleProductValidationRequest_WhenProductDoesNotExist_SendsFalseResponse() {
        when(inventoryService.getInventory(PRODUCT_ID))
                .thenReturn(Optional.empty());

        ProductValidationRequest request = new ProductValidationRequest(REQUEST_ID, PRODUCT_ID);

        consumer.handleProductValidationRequest(request);

        verify(inventoryService).getInventory(PRODUCT_ID);

        ArgumentCaptor<ProductValidationResponse> responseCaptor = ArgumentCaptor
                .forClass(ProductValidationResponse.class);

        verify(kafkaTemplate).send(
                eq(RESPONSE_TOPIC),
                eq(REQUEST_ID),
                responseCaptor.capture());

        ProductValidationResponse response = responseCaptor.getValue();

        assertEquals(REQUEST_ID, response.getRequestId());
        assertEquals(PRODUCT_ID, response.getProductId());
        assertFalse(response.isExists());
    }

    @Test
    void handleProductValidationRequest_UsesRequestIdAsKafkaMessageKey() {
        when(inventoryService.getInventory(PRODUCT_ID))
                .thenReturn(Optional.empty());

        ProductValidationRequest request = new ProductValidationRequest(REQUEST_ID, PRODUCT_ID);

        consumer.handleProductValidationRequest(request);

        verify(kafkaTemplate).send(
                eq(RESPONSE_TOPIC),
                eq(REQUEST_ID),
                any(ProductValidationResponse.class));
    }
}