package com.skmcore.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skmcore.orderservice.dto.AddItemRequest;
import com.skmcore.orderservice.dto.OrderItemResponse;
import com.skmcore.orderservice.service.OrderItemService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class OrderItemControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private OrderItemService orderItemService;

    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private UUID orderId;
    private UUID itemId;
    private UUID productId;
    private OrderItemResponse orderItemResponse;
    private AddItemRequest addItemRequest;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        orderId = UUID.randomUUID();
        itemId = UUID.randomUUID();
        productId = UUID.randomUUID();
        addItemRequest = new AddItemRequest(productId, 2);
        orderItemResponse = new OrderItemResponse(
            itemId,
            productId,
            "Test Product",
            2,
            new BigDecimal("25.00"),
            LocalDateTime.now()
        );
    }

    @Test
    void getItemsByOrderId_ShouldReturnOrderItems() throws Exception {
        when(orderItemService.getItemsByOrderId(orderId)).thenReturn(List.of(orderItemResponse));

        mockMvc.perform(get("/api/v1/orders/{orderId}/items", orderId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemId.toString()))
                .andExpect(jsonPath("$[0].productName").value("Test Product"))
                .andExpect(jsonPath("$[0].quantity").value(2));
    }

    @Test
    void getItemsByOrderId_WhenNoItems_ShouldReturnEmptyList() throws Exception {
        when(orderItemService.getItemsByOrderId(orderId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/orders/{orderId}/items", orderId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void addItemToOrder_ShouldReturnCreated() throws Exception {
        when(orderItemService.addItemToOrder(eq(orderId), any(AddItemRequest.class)))
            .thenReturn(orderItemResponse);

        mockMvc.perform(post("/api/v1/orders/{orderId}/items", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addItemRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(productId.toString()))
                .andExpect(jsonPath("$.productName").value("Test Product"))
                .andExpect(jsonPath("$.quantity").value(2));
    }

    @Test
    void removeItemFromOrder_ShouldReturnNoContent() throws Exception {
        doNothing().when(orderItemService).removeItemFromOrder(eq(orderId), eq(itemId));

        mockMvc.perform(delete("/api/v1/orders/{orderId}/items/{itemId}", orderId, itemId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(orderItemService).removeItemFromOrder(orderId, itemId);
    }
}
