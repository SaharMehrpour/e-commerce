package com.ecommerce.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ecommerce.order.controller.OrderController;
import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.UpdateOrderRequest;
import com.ecommerce.order.service.OrderService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
        createdOrder.setStatus(OrderStatus.CREATED);

        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(createdOrder);

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
                .andExpect(jsonPath("$.status").value(OrderStatus.CREATED.name()));
    }

    @Test
    void shouldGetOrders() throws Exception {
        List<Order> mockOrders = List.of(
            new Order("u1", "p1", 2),
            new Order("u2", "p2", 5)
        );

        mockOrders.get(0).setStatus(OrderStatus.CREATED);
        mockOrders.get(1).setStatus(OrderStatus.CREATED);

        when(orderService.getOrders()).thenReturn(mockOrders);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId").value("u1"))
                .andExpect(jsonPath("$[0].productId").value("p1"))
                .andExpect(jsonPath("$[0].quantity").value(2))
                .andExpect(jsonPath("$[0].status").value(OrderStatus.CREATED.name()))
                .andExpect(jsonPath("$[1].userId").value("u2"))
                .andExpect(jsonPath("$[1].productId").value("p2"))
                .andExpect(jsonPath("$[1].quantity").value(5))
                .andExpect(jsonPath("$[1].status").value(OrderStatus.CREATED.name()));
    }

    @Test
    void shouldReturnOrderWhenFound() throws Exception {
        Order order = new Order("u1", "p1", 2);
        order.setStatus(OrderStatus.CREATED);

        when(orderService.getOrder(any(String.class))).thenReturn(Optional.of(order));

        mockMvc.perform(get("/orders/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value("u1"))
            .andExpect(jsonPath("$.productId").value("p1"))
            .andExpect(jsonPath("$.quantity").value(2))
            .andExpect(jsonPath("$.status").value(OrderStatus.CREATED.name()));

    }

    @Test
    void shouldReturn404WhenNotFound() throws Exception {
        when(orderService.getOrder(any(String.class))).thenReturn(Optional.empty());

        mockMvc.perform(get("/orders/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldCancelOrderWhenFound() throws Exception {
        Order order = new Order("u1", "p1", 2);
        order.setStatus(OrderStatus.CANCELLED);

        when(orderService.cancelOrder(any(String.class))).thenReturn(Optional.of(order));

        mockMvc.perform(patch("/orders/1/cancel"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value("u1"))
            .andExpect(jsonPath("$.productId").value("p1"))
            .andExpect(jsonPath("$.quantity").value(2))
            .andExpect(jsonPath("$.status").value(OrderStatus.CANCELLED.name()));

    }

    @Test
    void shouldReturn404WhenCancelledOrderNotFound() throws Exception {
        when(orderService.cancelOrder(any(String.class))).thenReturn(Optional.empty());

        mockMvc.perform(patch("/orders/999/cancel"))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateOrderFields() throws Exception {

        Order order = new Order("u1", "updated-product", 10);
        order.setStatus(OrderStatus.CREATED);

        when(orderService.updateOrder(any(String.class), any(UpdateOrderRequest.class)))
                .thenReturn(Optional.of(order));

        String updateJson = """
            {
                "quantity": 10,
                "productId": "updated-product"
            }
            """;

        mockMvc.perform(patch("/orders/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("u1"))
                .andExpect(jsonPath("$.productId").value("updated-product"))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.status").value(OrderStatus.CREATED.name()));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingOrder() throws Exception {

        when(orderService.updateOrder(any(String.class), any(UpdateOrderRequest.class)))
                .thenReturn(Optional.empty());

        String updateJson = """
            {
                "quantity": 10
            }
            """;

        mockMvc.perform(patch("/orders/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isNotFound());
    }
}
