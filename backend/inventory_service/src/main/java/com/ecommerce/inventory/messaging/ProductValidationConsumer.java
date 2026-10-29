package com.ecommerce.inventory.messaging;

import com.ecommerce.inventory.service.InventoryService;
import com.ecommerce.shared.event.inventory.ProductValidationRequest;
import com.ecommerce.shared.event.inventory.ProductValidationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProductValidationConsumer {

        private final InventoryService inventoryService;
        private final KafkaTemplate<String, Object> kafkaTemplate;
        private final String productValidationResponseTopic;

        public ProductValidationConsumer(
                        InventoryService inventoryService,
                        KafkaTemplate<String, Object> kafkaTemplate,
                        @Value("${app.kafka.topics.product-validation-responses}") String productValidationResponseTopic) {
                this.inventoryService = inventoryService;
                this.kafkaTemplate = kafkaTemplate;
                this.productValidationResponseTopic = productValidationResponseTopic;
        }

        @KafkaListener(topics = "${app.kafka.topics.product-validation-requests}", groupId = "inventory-product-validation-group", containerFactory = "productValidationRequestKafkaListenerFactory"

        )
        public void handleProductValidationRequest(ProductValidationRequest request) {
                boolean exists = inventoryService
                                .getInventory(request.getProductId())
                                .isPresent();

                ProductValidationResponse response = new ProductValidationResponse(
                                request.getRequestId(),
                                request.getProductId(),
                                exists);

                kafkaTemplate.send(
                                productValidationResponseTopic,
                                request.getRequestId(),
                                response);
        }
}