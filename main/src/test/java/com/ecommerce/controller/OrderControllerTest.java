package com.ecommerce.controller;

import com.ecommerce.model.Order;
import com.ecommerce.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void shouldCreateOrder() throws Exception {
        Order createdOrder = new Order("u1", "p1", 2);
        createdOrder.setStatus("CREATED");

        when(orderService.createOrder(any(Order.class))).thenReturn(createdOrder);

        String orderJson = """
            {
                "userId": "u1",
                "productId": "p1",
                "quantity": 2
            }
            """;

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("u1"))
                .andExpect(jsonPath("$.productId").value("p1"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void shouldGetOrders() throws Exception {
        List<Order> mockOrders = List.of(
            new Order("u1", "p1", 2),
            new Order("u2", "p2", 5)
        );

        mockOrders.get(0).setStatus("CREATED");
        mockOrders.get(1).setStatus("CREATED");

        when(orderService.getOrders()).thenReturn(mockOrders);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId").value("u1"))
                .andExpect(jsonPath("$[0].productId").value("p1"))
                .andExpect(jsonPath("$[0].quantity").value(2))
                .andExpect(jsonPath("$[0].status").value("CREATED"))
                .andExpect(jsonPath("$[1].userId").value("u2"))
                .andExpect(jsonPath("$[1].productId").value("p2"))
                .andExpect(jsonPath("$[1].quantity").value(5))
                .andExpect(jsonPath("$[1].status").value("CREATED"));
    }

    @Test
    void shouldReturnOrderWhenFound() throws Exception {
        Order order = new Order("u1", "p1", 2);
        order.setStatus("CREATED");

        when(orderService.getOrder("1")).thenReturn(Optional.of(order));

        mockMvc.perform(get("/orders/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value("u1"))
            .andExpect(jsonPath("$.productId").value("p1"))
            .andExpect(jsonPath("$.quantity").value(2))
            .andExpect(jsonPath("$.status").value("CREATED"));

    }

    @Test
    void shouldReturn404WhenNotFound() throws Exception {
        
        when(orderService.getOrder("999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/orders/999"))
            .andExpect(status().isNotFound());
    }
}
