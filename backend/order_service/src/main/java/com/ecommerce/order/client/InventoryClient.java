package com.ecommerce.order.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.ecommerce.shared.dto.inventory.InventoryResponse;

import org.springframework.web.client.HttpClientErrorException;

@Component
public class InventoryClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public InventoryClient(
            RestTemplate restTemplate,
            @Value("${inventory.service.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public InventoryResponse getInventory(String productId) {
        try {
            return restTemplate.getForObject(
                    baseUrl + "/" + productId,
                    InventoryResponse.class);
        } catch (HttpClientErrorException.NotFound ex) {
            return null;
        }
    }
}