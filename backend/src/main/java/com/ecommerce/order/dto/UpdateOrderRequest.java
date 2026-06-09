package com.ecommerce.order.dto;

import jakarta.validation.constraints.Min;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UpdateOrderRequest {

    @Min(value = 1, message = "Quantity must be greater than zero")
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
