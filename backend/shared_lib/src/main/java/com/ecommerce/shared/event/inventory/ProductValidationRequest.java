package com.ecommerce.shared.event.inventory;

public class ProductValidationRequest {

    private String requestId;
    private String productId;

    public ProductValidationRequest() {
    }

    public ProductValidationRequest(String requestId, String productId) {
        this.requestId = requestId;
        this.productId = productId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}