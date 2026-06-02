package com.ecommerce.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void shouldHandleOrderNotFoundException() {

        OrderNotFoundException ex =
                new OrderNotFoundException("Order not found");

        ResponseEntity<?> response =
                handler.handleOrderNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        Map<?, ?> body = (Map<?, ?>) response.getBody();

        assertEquals("Order Not Found", body.get("error"));
        assertEquals("Order not found", body.get("message"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    void shouldHandleOrderAlreadyCancelledException() {

        OrderAlreadyCancelledException ex =
                new OrderAlreadyCancelledException("Already cancelled");

        ResponseEntity<?> response =
                handler.handleOrderAlreadyCancelledException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        Map<?, ?> body = (Map<?, ?>) response.getBody();

        assertEquals("Order Already Cancelled", body.get("error"));
        assertEquals("Already cancelled", body.get("message"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    void shouldHandleInvalidOrderException() {

        InvalidOrderException ex =
                new InvalidOrderException("Invalid quantity");

        ResponseEntity<?> response =
                handler.handleInvalidOrderException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<?, ?> body = (Map<?, ?>) response.getBody();

        assertEquals("Invalid Order", body.get("error"));
        assertEquals("Invalid quantity", body.get("message"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    void shouldHandleOrderNotUpdatableException() {

        OrderNotUpdatableException ex =
                new OrderNotUpdatableException("Cannot update order");

        ResponseEntity<?> response =
                handler.handleOrderNotUpdatableException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        Map<?, ?> body = (Map<?, ?>) response.getBody();

        assertEquals("Order Update Failed", body.get("error"));
        assertEquals("Cannot update order", body.get("message"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    void shouldHandleInventoryNotFoundException() {
        InventoryNotFoundException ex =
                new InventoryNotFoundException("Inventory not found");

        ResponseEntity<?> response =
                handler.handleInventoryNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        Map<?, ?> body = (Map<?, ?>) response.getBody();

        assertEquals("Inventory Not Found", body.get("error"));
        assertEquals("Inventory not found", body.get("message"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    void shouldHandleInventoryNotEnoughException() {
        InventoryNotEnoughException ex = new InventoryNotEnoughException("Not enough inventory");
        
        ResponseEntity<?> response =
                handler.handleInventoryNotEnoughException(ex);
        
        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode());
        
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        
        assertEquals("Insufficient Stock", body.get("error"));
        assertEquals("Not enough inventory", body.get("message"));
        assertNotNull(body.get("timestamp"));
    }
}