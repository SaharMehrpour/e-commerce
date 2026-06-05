package com.ecommerce.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.ecommerce.order.controller.OrderController;
import com.ecommerce.order.domain.Order;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.UpdateOrderRequest;
import com.ecommerce.order.service.OrderService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestOrderService orderService;

    @BeforeEach
    void setUp() {
        orderService.orders = new ArrayList<>();
        orderService.order = Optional.empty();
    }

    @Test
    void shouldCreateOrder() throws Exception {
        Order createdOrder = new Order("u1", "p1", 2);
        createdOrder.setStatus("CREATED");

        orderService.orderToCreate = createdOrder;

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

        orderService.orders = mockOrders;

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

        orderService.order = Optional.of(order);

        mockMvc.perform(get("/orders/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value("u1"))
            .andExpect(jsonPath("$.productId").value("p1"))
            .andExpect(jsonPath("$.quantity").value(2))
            .andExpect(jsonPath("$.status").value("CREATED"));

    }

    @Test
    void shouldReturn404WhenNotFound() throws Exception {
        orderService.order = Optional.empty();

        mockMvc.perform(get("/orders/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldCancelOrderWhenFound() throws Exception {
        Order order = new Order("u1", "p1", 2);
        order.setStatus("CREATED");

        orderService.order = Optional.of(order);

        mockMvc.perform(patch("/orders/1/cancel"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value("u1"))
            .andExpect(jsonPath("$.productId").value("p1"))
            .andExpect(jsonPath("$.quantity").value(2))
            .andExpect(jsonPath("$.status").value("CANCELLED"));

    }

    @Test
    void shouldReturn404WhenCancelledOrderNotFound() throws Exception {
        orderService.order = Optional.empty();

        mockMvc.perform(patch("/orders/999/cancel"))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateOrderFields() throws Exception {

        Order order = new Order("u1", "updated-product", 10);
        order.setStatus("CREATED");

        orderService.order = Optional.of(order);

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
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingOrder() throws Exception {

        orderService.order = Optional.empty();

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

    @TestConfiguration
    static class TestConfig {

        @Bean
        TestOrderService orderService() {
            return new TestOrderService();
        }
    }

    static class TestOrderService extends OrderService {

        private Order orderToCreate;
        private List<Order> orders = new ArrayList<>();
        private Optional<Order> order = Optional.empty();

        TestOrderService() {
            super(null, null, null);
        }

        @Override
        public Order createOrder(CreateOrderRequest request) {
            return orderToCreate;
        }

        @Override
        public List<Order> getOrders() {
            return orders;
        }

        @Override
        public Optional<Order> getOrder(String id) {
            return order;
        }

        @Override
        public Optional<Order> cancelOrder(String id) {
            order.ifPresent(o -> o.setStatus("CANCELLED"));
            return order;
        }

        @Override
        public Optional<Order> updateOrder(String id, UpdateOrderRequest request) {

            order.ifPresent(o -> {
                if (request.getQuantity() != null) {
                    o.setQuantity(request.getQuantity());
                }
                if (request.getProductId() != null) {
                    o.setProductId(request.getProductId());
                }
            });

            return order;
        }
    }
}
