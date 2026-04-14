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
            .customerName("Jane Doe")
            .totalAmount(new BigDecimal("120.00"))
            .status(Order.OrderStatus.PENDING)
            .build();
        Order savedOrder = orderRepository.save(order);

        Optional<Order> found = orderRepository.findById(savedOrder.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getCustomerName()).isEqualTo("Jane Doe");
    }

    @Test
    void existsById_ShouldReturnTrueWhenExists() {
        Order order = Order.builder()
            .customerName("John Smith")
            .totalAmount(new BigDecimal("50.00"))
            .status(Order.OrderStatus.PENDING)
            .build();
        Order savedOrder = orderRepository.save(order);

        boolean exists = orderRepository.existsById(savedOrder.getId());

        assertThat(exists).isTrue();
    }
}
