package com.ecommerce.order.messaging;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.ecommerce.shared.event.inventory.ProductValidationRequest;
import com.ecommerce.shared.event.inventory.ProductValidationResponse;

@Service
public class ProductValidationClient {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String requestTopic;

    private final ConcurrentHashMap<String, CompletableFuture<ProductValidationResponse>> pendingRequests = new ConcurrentHashMap<>();

    public ProductValidationClient(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topics.product-validation-requests}") String requestTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.requestTopic = requestTopic;
    }

    public boolean validateProduct(String productId) {
        String requestId = UUID.randomUUID().toString();

        CompletableFuture<ProductValidationResponse> responseFuture = new CompletableFuture<>();

        pendingRequests.put(requestId, responseFuture);

        try {
            kafkaTemplate.send(
                    requestTopic,
                    requestId,
                    new ProductValidationRequest(requestId, productId))
                    .get(5, TimeUnit.SECONDS);

            ProductValidationResponse response = responseFuture.get(5, TimeUnit.SECONDS);

            return response.isExists();

        } catch (TimeoutException e) {
            throw new IllegalStateException(
                    "Timed out sending or waiting for product validation", e);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Interrupted while validating product", e);

        } catch (ExecutionException e) {
            throw new IllegalStateException(
                    "Failed to send request or validate product",
                    e.getCause());

        } finally {
            pendingRequests.remove(requestId);
        }
    }

    @KafkaListener(topics = "${app.kafka.topics.product-validation-responses}", groupId = "order-product-validation-group", containerFactory = "productValidationResponseKafkaListenerFactory")
    public void handleProductValidationResponse(
            ProductValidationResponse response) {

        CompletableFuture<ProductValidationResponse> future = pendingRequests.get(response.getRequestId());

        if (future != null) {
            future.complete(response);
        }
    }
}