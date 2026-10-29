package com.ecommerce.shared.event.inventory;

public class ProductValidationResponse {

    private String requestId;
    private String productId;
    private boolean exists;

    public ProductValidationResponse() {
    }

    public ProductValidationResponse(
            String requestId,
            String productId,
            boolean exists) {
        this.requestId = requestId;
        this.productId = productId;
        this.exists = exists;
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

    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }
}