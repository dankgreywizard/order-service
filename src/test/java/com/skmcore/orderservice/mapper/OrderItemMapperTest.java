package com.skmcore.orderservice.mapper;

import com.skmcore.orderservice.dto.OrderItemResponse;
import com.skmcore.orderservice.model.OrderItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = OrderItemMapperImpl.class)
class OrderItemMapperTest {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Test
    void toResponse_ShouldMapOrderItemToOrderItemResponse() {
        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        OrderItem orderItem = OrderItem.builder()
            .id(id)
            .orderId(UUID.randomUUID())
            .productId(productId)
            .productName("Test Product")
            .quantity(2)
            .unitPrice(new BigDecimal("25.00"))
            .createdAt(now)
            .build();

        OrderItemResponse response = orderItemMapper.toResponse(orderItem);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.productId()).isEqualTo(productId);
        assertThat(response.productName()).isEqualTo("Test Product");
        assertThat(response.quantity()).isEqualTo(2);
        assertThat(response.unitPrice()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(response.createdAt()).isEqualTo(now);
    }
}
