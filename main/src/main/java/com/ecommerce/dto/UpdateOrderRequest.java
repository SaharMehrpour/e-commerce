package com.ecommerce.dto;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UpdateOrderRequest {

    private Integer quantity;
    private String productId;

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}