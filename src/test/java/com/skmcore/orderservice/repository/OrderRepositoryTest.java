package com.skmcore.orderservice.repository;

import com.skmcore.orderservice.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void saveAndFind_ShouldReturnOrder() {
        Order order = Order.builder()
            .orderNumber("ORD-TEST-001")
            .customerId("customer-123")
            .currency("USD")
            .totalAmount(new BigDecimal("120.00"))
            .status(Order.OrderStatus.PENDING)
            .build();
        Order savedOrder = orderRepository.save(order);

        Optional<Order> found = orderRepository.findById(savedOrder.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getCustomerId()).isEqualTo("customer-123");
        assertThat(found.get().getCurrency()).isEqualTo("USD");
        assertThat(found.get().getOrderNumber()).isEqualTo("ORD-TEST-001");
    }

    @Test
    void existsById_ShouldReturnTrueWhenExists() {
        Order order = Order.builder()
            .orderNumber("ORD-TEST-002")
            .customerId("customer-456")
            .currency("USD")
            .totalAmount(new BigDecimal("50.00"))
            .status(Order.OrderStatus.PENDING)
            .build();
        Order savedOrder = orderRepository.save(order);

        boolean exists = orderRepository.existsById(savedOrder.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void saveOrder_WithAllFields_ShouldPersistCorrectly() {
        Order order = Order.builder()
            .orderNumber("ORD-TEST-003")
            .customerId("customer-789")
            .currency("EUR")
            .totalAmount(new BigDecimal("250.50"))
            .status(Order.OrderStatus.PROCESSING)
            .build();
        Order savedOrder = orderRepository.save(order);

        Optional<Order> found = orderRepository.findById(savedOrder.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getOrderNumber()).isEqualTo("ORD-TEST-003");
        assertThat(found.get().getCustomerId()).isEqualTo("customer-789");
        assertThat(found.get().getCurrency()).isEqualTo("EUR");
        assertThat(found.get().getTotalAmount()).isEqualByComparingTo(new BigDecimal("250.50"));
        assertThat(found.get().getStatus()).isEqualTo(Order.OrderStatus.PROCESSING);
    }
}
