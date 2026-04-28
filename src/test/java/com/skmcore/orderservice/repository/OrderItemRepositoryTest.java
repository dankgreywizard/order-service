package com.skmcore.orderservice.repository;

import com.skmcore.orderservice.model.Order;
import com.skmcore.orderservice.model.OrderItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class OrderItemRepositoryTest {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void findByOrderId_ShouldReturnOrderItems() {
        Order order = Order.builder()
            .orderNumber("ORD-ITEM-TEST-001")
            .customerId("customer-123")
            .currency("USD")
            .totalAmount(new BigDecimal("150.00"))
            .status(Order.OrderStatus.PENDING)
            .build();
        Order savedOrder = orderRepository.save(order);

        OrderItem item1 = OrderItem.builder()
            .orderId(savedOrder.getId())
            .productId(UUID.randomUUID())
            .productName("Product 1")
            .quantity(2)
            .unitPrice(new BigDecimal("50.00"))
            .build();

        OrderItem item2 = OrderItem.builder()
            .orderId(savedOrder.getId())
            .productId(UUID.randomUUID())
            .productName("Product 2")
            .quantity(1)
            .unitPrice(new BigDecimal("50.00"))
            .build();

        orderItemRepository.save(item1);
        orderItemRepository.save(item2);

        List<OrderItem> items = orderItemRepository.findByOrderId(savedOrder.getId());

        assertThat(items).hasSize(2);
        assertThat(items).extracting("orderId").containsOnly(savedOrder.getId());
    }

    @Test
    void findByOrderId_WhenNoItems_ShouldReturnEmptyList() {
        Order order = Order.builder()
            .orderNumber("ORD-ITEM-TEST-002")
            .customerId("customer-456")
            .currency("USD")
            .totalAmount(new BigDecimal("0.00"))
            .status(Order.OrderStatus.PENDING)
            .build();
        Order savedOrder = orderRepository.save(order);

        List<OrderItem> items = orderItemRepository.findByOrderId(savedOrder.getId());

        assertThat(items).isEmpty();
    }

    @Test
    void countByOrderId_ShouldReturnCorrectCount() {
        Order order = Order.builder()
            .orderNumber("ORD-ITEM-TEST-003")
            .customerId("customer-789")
            .currency("USD")
            .totalAmount(new BigDecimal("200.00"))
            .status(Order.OrderStatus.PENDING)
            .build();
        Order savedOrder = orderRepository.save(order);

        OrderItem item1 = OrderItem.builder()
            .orderId(savedOrder.getId())
            .productId(UUID.randomUUID())
            .productName("Product 1")
            .quantity(1)
            .unitPrice(new BigDecimal("100.00"))
            .build();

        OrderItem item2 = OrderItem.builder()
            .orderId(savedOrder.getId())
            .productId(UUID.randomUUID())
            .productName("Product 2")
            .quantity(2)
            .unitPrice(new BigDecimal("50.00"))
            .build();

        OrderItem item3 = OrderItem.builder()
            .orderId(savedOrder.getId())
            .productId(UUID.randomUUID())
            .productName("Product 3")
            .quantity(1)
            .unitPrice(new BigDecimal("50.00"))
            .build();

        orderItemRepository.save(item1);
        orderItemRepository.save(item2);
        orderItemRepository.save(item3);

        long count = orderItemRepository.countByOrderId(savedOrder.getId());

        assertThat(count).isEqualTo(3);
    }

    @Test
    void saveAndFind_ShouldReturnOrderItem() {
        Order order = Order.builder()
            .orderNumber("ORD-ITEM-TEST-004")
            .customerId("customer-999")
            .currency("USD")
            .totalAmount(new BigDecimal("75.00"))
            .status(Order.OrderStatus.PENDING)
            .build();
        Order savedOrder = orderRepository.save(order);

        OrderItem item = OrderItem.builder()
            .orderId(savedOrder.getId())
            .productId(UUID.randomUUID())
            .productName("Test Product")
            .quantity(3)
            .unitPrice(new BigDecimal("25.00"))
            .build();

        OrderItem savedItem = orderItemRepository.save(item);

        OrderItem foundItem = orderItemRepository.findById(savedItem.getId()).orElse(null);

        assertThat(foundItem).isNotNull();
        assertThat(foundItem.getProductName()).isEqualTo("Test Product");
        assertThat(foundItem.getQuantity()).isEqualTo(3);
        assertThat(foundItem.getUnitPrice()).isEqualByComparingTo(new BigDecimal("25.00"));
    }

    @Test
    void deleteById_ShouldDeleteOrderItem() {
        Order order = Order.builder()
            .orderNumber("ORD-ITEM-TEST-005")
            .customerId("customer-delete")
            .currency("USD")
            .totalAmount(new BigDecimal("50.00"))
            .status(Order.OrderStatus.PENDING)
            .build();
        Order savedOrder = orderRepository.save(order);

        OrderItem item = OrderItem.builder()
            .orderId(savedOrder.getId())
            .productId(UUID.randomUUID())
            .productName("To Delete")
            .quantity(1)
            .unitPrice(new BigDecimal("50.00"))
            .build();

        OrderItem savedItem = orderItemRepository.save(item);

        orderItemRepository.deleteById(savedItem.getId());

        assertThat(orderItemRepository.findById(savedItem.getId())).isEmpty();
    }
}
