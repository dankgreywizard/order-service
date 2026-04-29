package com.skmcore.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skmcore.orderservice.dto.OrderRequest;
import com.skmcore.orderservice.dto.OrderResponse;
import com.skmcore.orderservice.model.Order;
import com.skmcore.orderservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class OrderControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private OrderService orderService;

    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void createOrder_ShouldReturnCreated() throws Exception {
        OrderRequest request = new OrderRequest("customer-123", "USD");
        OrderResponse response = new OrderResponse(UUID.randomUUID(), "ORD-12345", "customer-123", "USD", new BigDecimal("100.00"), Order.OrderStatus.PENDING, List.of(), LocalDateTime.now(), LocalDateTime.now());

        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value("customer-123"))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.orderNumber").value("ORD-12345"));
    }

    @Test
    void createOrder_WithInvalidCustomerId_ShouldReturnBadRequest() throws Exception {
        OrderRequest request = new OrderRequest("C", "USD"); // too short

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.customerId").exists());
    }

    @Test
    void createOrder_WithInvalidCurrency_ShouldReturnBadRequest() throws Exception {
        OrderRequest request = new OrderRequest("customer-123", "US"); // too short, must be 3

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.currency").exists());
    }

    @Test
    void getOrderById_ShouldReturnOrder() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderResponse response = new OrderResponse(orderId, "ORD-12345", "customer-123", "USD", new BigDecimal("100.00"), Order.OrderStatus.PENDING, List.of(), LocalDateTime.now(), LocalDateTime.now());

        when(orderService.getOrderById(orderId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.customerId").value("customer-123"))
                .andExpect(jsonPath("$.orderNumber").value("ORD-12345"));
    }

    @Test
    void getAllOrders_ShouldReturnList() throws Exception {
        OrderResponse response = new OrderResponse(UUID.randomUUID(), "ORD-12345", "customer-123", "USD", new BigDecimal("100.00"), Order.OrderStatus.PENDING, List.of(), LocalDateTime.now(), LocalDateTime.now());

        when(orderService.getAllOrders()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value("customer-123"))
                .andExpect(jsonPath("$[0].orderNumber").value("ORD-12345"));
    }

    @Test
    void updateOrderStatus_ShouldReturnUpdatedOrder() throws Exception {
        UUID orderId = UUID.randomUUID();
        Order.OrderStatus newStatus = Order.OrderStatus.SHIPPED;
        OrderResponse response = new OrderResponse(orderId, "ORD-12345", "customer-123", "USD", new BigDecimal("100.00"), newStatus, List.of(), LocalDateTime.now(), LocalDateTime.now());

        when(orderService.updateOrderStatus(eq(orderId), eq(newStatus))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/orders/{id}/status", orderId)
                .param("status", newStatus.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(newStatus.name()));
    }

    @Test
    void deleteOrder_ShouldReturnNoContent() throws Exception {
        UUID orderId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/orders/{id}", orderId))
                .andExpect(status().isNoContent());
    }
}
