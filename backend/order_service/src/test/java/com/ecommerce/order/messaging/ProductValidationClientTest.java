package com.ecommerce.order.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import com.ecommerce.shared.event.inventory.ProductValidationRequest;
import com.ecommerce.shared.event.inventory.ProductValidationResponse;

@ExtendWith(MockitoExtension.class)
class ProductValidationClientTest {

    private static final String REQUEST_TOPIC = "product-validation-requests";
    private static final String PRODUCT_ID = "product-1";

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private ProductValidationClient productValidationClient;

    @BeforeEach
    void setUp() {
        productValidationClient = new ProductValidationClient(kafkaTemplate, REQUEST_TOPIC);
    }

    @Test
    void validateProduct_WhenProductExists_ReturnsTrue() throws Exception {
        when(kafkaTemplate.send(
                eq(REQUEST_TOPIC),
                any(String.class),
                any(ProductValidationRequest.class)))
                .thenAnswer(invocation -> {
                    String requestId = invocation.getArgument(1);

                    ProductValidationResponse response = new ProductValidationResponse(
                            requestId, PRODUCT_ID, true);

                    productValidationClient.handleProductValidationResponse(
                            response);

                    return successfulSendResult();
                });

        boolean exists = productValidationClient.validateProduct(PRODUCT_ID);

        assertTrue(exists);

        ArgumentCaptor<ProductValidationRequest> requestCaptor = ArgumentCaptor
                .forClass(ProductValidationRequest.class);

        verify(kafkaTemplate).send(
                eq(REQUEST_TOPIC),
                any(String.class),
                requestCaptor.capture());

        ProductValidationRequest request = requestCaptor.getValue();

        assertEquals(PRODUCT_ID, request.getProductId());
        assertTrue(
                request.getRequestId() != null
                        && !request.getRequestId().isBlank());
    }

    @Test
    void validateProduct_WhenProductDoesNotExist_ReturnsFalse() throws Exception {
        when(kafkaTemplate.send(
                eq(REQUEST_TOPIC),
                any(String.class),
                any(ProductValidationRequest.class)))
                .thenAnswer(invocation -> {
                    String requestId = invocation.getArgument(1);

                    productValidationClient.handleProductValidationResponse(
                            new ProductValidationResponse(
                                    requestId, PRODUCT_ID, false));

                    return successfulSendResult();
                });

        boolean exists = productValidationClient.validateProduct(PRODUCT_ID);

        assertFalse(exists);
    }

    @Test
    void validateProduct_WhenResponseHasDifferentRequestId_TimesOut()
            throws Exception {
        when(kafkaTemplate.send(
                eq(REQUEST_TOPIC),
                any(String.class),
                any(ProductValidationRequest.class)))
                .thenAnswer(invocation -> {
                    productValidationClient.handleProductValidationResponse(
                            new ProductValidationResponse(
                                    "different-request-id", PRODUCT_ID, true));

                    return successfulSendResult();
                });

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> productValidationClient.validateProduct(PRODUCT_ID));

        assertTrue(exception.getMessage().contains("Timed out"));
    }

    @Test
    void validateProduct_WhenSendingRequestFails_ThrowsIllegalStateException()
            throws Exception {
        CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();

        failedFuture.completeExceptionally(
                new IllegalArgumentException("Kafka send failed"));

        when(kafkaTemplate.send(
                eq(REQUEST_TOPIC),
                any(String.class),
                any(ProductValidationRequest.class)))
                .thenReturn(failedFuture);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> productValidationClient.validateProduct(PRODUCT_ID));

        assertTrue(exception.getMessage().contains("Failed to send request"));
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void handleProductValidationResponse_WhenNoMatchingRequestExists_DoesNotFail() {
        ProductValidationResponse response = new ProductValidationResponse(
                "unknown-request-id", PRODUCT_ID, true);

        // A late or unrelated response should be safely ignored.
        productValidationClient.handleProductValidationResponse(response);
    }

    private void mockSuccessfulSend() throws Exception {
        when(kafkaTemplate.send(
                eq(REQUEST_TOPIC),
                any(String.class),
                any(ProductValidationRequest.class)))
                .thenAnswer(invocation -> {
                    String requestId = invocation.getArgument(1);

                    productValidationClient.handleProductValidationResponse(
                            new ProductValidationResponse(
                                    requestId, PRODUCT_ID, true));

                    return successfulSendResult();
                });
    }

    @SuppressWarnings("unchecked")
    private CompletableFuture<SendResult<String, Object>> successfulSendResult() {
        return CompletableFuture.completedFuture(
                mock(SendResult.class));
    }
}
