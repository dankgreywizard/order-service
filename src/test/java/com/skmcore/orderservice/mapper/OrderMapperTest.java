package com.skmcore.orderservice.mapper;

import com.skmcore.orderservice.dto.OrderRequest;
import com.skmcore.orderservice.dto.OrderResponse;
import com.skmcore.orderservice.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = OrderMapperImpl.class)
class OrderMapperTest {

    @Autowired
    private OrderMapper orderMapper;

    @Test
    void toEntity_ShouldMapOrderRequestToOrder() {
        OrderRequest request = new OrderRequest("Jane Smith", new BigDecimal("150.50"));

        Order order = orderMapper.toEntity(request);

        assertThat(order).isNotNull();
        assertThat(order.getCustomerName()).isEqualTo("Jane Smith");
        assertThat(order.getTotalAmount()).isEqualTo(new BigDecimal("150.50"));
        assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.PENDING);
        assertThat(order.getId()).isNull();
    }

    @Test
    void toResponse_ShouldMapOrderToOrderResponse() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Order order = Order.builder()
            .id(id)
            .customerName("John Doe")
            .totalAmount(new BigDecimal("100.00"))
            .status(Order.OrderStatus.PROCESSING)
            .createdAt(now)
            .updatedAt(now)
            .build();

        OrderResponse response = orderMapper.toResponse(order);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.customerName()).isEqualTo("John Doe");
        assertThat(response.totalAmount()).isEqualTo(new BigDecimal("100.00"));
        assertThat(response.status()).isEqualTo(Order.OrderStatus.PROCESSING);
        assertThat(response.createdAt()).isEqualTo(now);
        assertThat(response.updatedAt()).isEqualTo(now);
    }
}
