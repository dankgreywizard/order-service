package com.skmcore.orderservice.event;

import com.skmcore.orderservice.jms.*;
import com.skmcore.orderservice.model.Order;
import com.skmcore.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderEventListenerTest {

    @Mock
    private JmsProducerService jmsProducerService;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderEventListener orderEventListener;

    private UUID orderId;
    private Order order;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        order = Order.builder()
                .id(orderId)
                .orderNumber("ORD-123")
                .customerId(UUID.randomUUID().toString())
                .currency("USD")
                .totalAmount(BigDecimal.TEN)
                .status(Order.OrderStatus.PENDING)
                .build();
    }

    @Test
    void handleOrderCreatedEvent_ShouldSendMessage() {
        OrderCreatedEvent event = new OrderCreatedEvent(orderId, order);

        orderEventListener.handleOrderCreatedEvent(event);

        verify(jmsProducerService).sendOrderCreatedEvent(any(OrderCreatedMessage.class));
    }

    @Test
    void handleOrderStatusChangedEvent_ShouldSendMessage() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        OrderStatusChangedEvent event = new OrderStatusChangedEvent(orderId, Order.OrderStatus.PENDING, Order.OrderStatus.PROCESSING);

        orderEventListener.handleOrderStatusChangedEvent(event);

        verify(jmsProducerService).sendOrderStatusChangedEvent(any(OrderStatusChangedMessage.class));
    }

    @Test
    void handleOrderItemAddedEvent_ShouldSendMessage() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        OrderItemAddedEvent event = new OrderItemAddedEvent(orderId, UUID.randomUUID(), UUID.randomUUID(), "Product", 2, BigDecimal.valueOf(5));

        orderEventListener.handleOrderItemAddedEvent(event);

        verify(jmsProducerService).sendOrderItemAddedEvent(any(OrderItemAddedMessage.class));
    }

    @Test
    void handleOrderItemRemovedEvent_ShouldSendMessage() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        OrderItemRemovedEvent event = new OrderItemRemovedEvent(orderId, UUID.randomUUID(), UUID.randomUUID(), "Product", 2, BigDecimal.valueOf(5), "Reason");

        orderEventListener.handleOrderItemRemovedEvent(event);

        verify(jmsProducerService).sendOrderItemRemovedEvent(any(OrderItemRemovedMessage.class));
    }
}
