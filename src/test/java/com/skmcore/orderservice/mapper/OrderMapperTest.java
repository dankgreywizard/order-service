package com.skmcore.orderservice.mapper;

import com.skmcore.orderservice.dto.OrderItemResponse;
import com.skmcore.orderservice.dto.OrderRequest;
import com.skmcore.orderservice.dto.OrderResponse;
import com.skmcore.orderservice.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = OrderMapperImpl.class)
class OrderMapperTest {

    @Autowired
    private OrderMapper orderMapper;

    @Test
    void toEntity_ShouldMapOrderRequestToOrder() {
        OrderRequest request = new OrderRequest("customer-123", "USD");

        Order order = orderMapper.toEntity(request);

        assertThat(order).isNotNull();
        assertThat(order.getCustomerId()).isEqualTo("customer-123");
        assertThat(order.getCurrency()).isEqualTo("USD");
        assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.PENDING);
        assertThat(order.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(order.getId()).isNull();
    }

    @Test
    void toResponse_WithOrderItems_ShouldMapOrderToOrderResponse() {
        UUID id = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Order order = Order.builder()
            .id(id)
            .orderNumber("ORD-12345")
            .customerId("customer-123")
            .currency("USD")
            .totalAmount(new BigDecimal("100.00"))
            .status(Order.OrderStatus.PROCESSING)
            .createdAt(now)
            .updatedAt(now)
            .build();

        OrderItemResponse itemResponse = new OrderItemResponse(itemId, UUID.randomUUID(), "Test Product", 2, new BigDecimal("50.00"), now);
        List<OrderItemResponse> items = List.of(itemResponse);

        OrderResponse response = orderMapper.toResponse(order, items);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.orderNumber()).isEqualTo("ORD-12345");
        assertThat(response.customerId()).isEqualTo("customer-123");
        assertThat(response.currency()).isEqualTo("USD");
        assertThat(response.totalAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(response.status()).isEqualTo(Order.OrderStatus.PROCESSING);
        assertThat(response.createdAt()).isEqualTo(now);
        assertThat(response.updatedAt()).isEqualTo(now);
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().productName()).isEqualTo("Test Product");
    }

    @Test
    void toResponse_WithoutOrderItems_ShouldMapOrderToOrderResponseWithNullItems() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Order order = Order.builder()
            .id(id)
            .orderNumber("ORD-12345")
            .customerId("customer-123")
            .currency("USD")
            .totalAmount(new BigDecimal("100.00"))
            .status(Order.OrderStatus.PROCESSING)
            .createdAt(now)
            .updatedAt(now)
            .build();

        OrderResponse response = orderMapper.toResponse(order);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.orderNumber()).isEqualTo("ORD-12345");
        assertThat(response.customerId()).isEqualTo("customer-123");
        assertThat(response.currency()).isEqualTo("USD");
        assertThat(response.totalAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(response.status()).isEqualTo(Order.OrderStatus.PROCESSING);
        assertThat(response.items()).isNull();
    }

    @Test
    void generateOrderNumber_ShouldReturnFormattedOrderNumber() {
        String orderNumber = orderMapper.generateOrderNumber();

        assertThat(orderNumber).isNotNull();
        assertThat(orderNumber).startsWith("ORD-");
        assertThat(orderNumber.length()).isEqualTo(12);
    }
}
