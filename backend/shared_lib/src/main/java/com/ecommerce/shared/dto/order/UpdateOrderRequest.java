package com.ecommerce.shared.dto.order;

import jakarta.validation.constraints.Min;

public class UpdateOrderRequest {

    @Min(value = 1, message = "Quantity must be greater than zero")
    private Integer quantity;
    private String productId;

    public UpdateOrderRequest() {}

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
