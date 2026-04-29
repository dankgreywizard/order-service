package com.skmcore.orderservice.jms;

import com.skmcore.orderservice.config.JmsConfig;
import com.skmcore.orderservice.model.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JmsProducerServiceTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @InjectMocks
    private JmsProducerService jmsProducerService;

    @Test
    void sendOrderCreatedEvent_ShouldSendMessage() {
        OrderCreatedMessage message = new OrderCreatedMessage(
                UUID.randomUUID(), "ORD-1", UUID.randomUUID(), "USD",
                BigDecimal.TEN, Order.OrderStatus.PENDING, 1, Instant.now());

        jmsProducerService.sendOrderCreatedEvent(message);

        verify(jmsTemplate).convertAndSend(eq(JmsConfig.ORDER_CREATED_QUEUE), eq(message));
    }

    @Test
    void sendOrderStatusChangedEvent_ShouldSendMessage() {
        OrderStatusChangedMessage message = new OrderStatusChangedMessage(
                UUID.randomUUID(), "ORD-1", Order.OrderStatus.PENDING,
                Order.OrderStatus.PROCESSING, Instant.now());

        jmsProducerService.sendOrderStatusChangedEvent(message);

        verify(jmsTemplate).convertAndSend(eq(JmsConfig.ORDER_STATUS_CHANGED_QUEUE), eq(message));
    }

    @Test
    void sendOrderItemAddedEvent_ShouldSendMessage() {
        OrderItemAddedMessage message = new OrderItemAddedMessage(
                UUID.randomUUID(), "ORD-1", UUID.randomUUID(), UUID.randomUUID(),
                "Product", 1, BigDecimal.TEN, BigDecimal.TEN, Instant.now());

        jmsProducerService.sendOrderItemAddedEvent(message);

        verify(jmsTemplate).convertAndSend(eq(JmsConfig.ORDER_ITEM_ADDED_QUEUE), eq(message));
    }

    @Test
    void sendOrderItemRemovedEvent_ShouldSendMessage() {
        OrderItemRemovedMessage message = new OrderItemRemovedMessage(
                UUID.randomUUID(), "ORD-1", UUID.randomUUID(), UUID.randomUUID(),
                "Product", 1, BigDecimal.TEN, BigDecimal.TEN, Instant.now(), "Reason");

        jmsProducerService.sendOrderItemRemovedEvent(message);

        verify(jmsTemplate).convertAndSend(eq(JmsConfig.ORDER_ITEM_REMOVED_QUEUE), eq(message));
    }

    @Test
    void sendProductCreatedEvent_ShouldSendMessage() {
        ProductCreatedMessage message = new ProductCreatedMessage(
                UUID.randomUUID(), "P1", "Product", "Desc", "Cat",
                BigDecimal.TEN, 100, true, Instant.now());

        jmsProducerService.sendProductCreatedEvent(message);

        verify(jmsTemplate).convertAndSend(eq(JmsConfig.PRODUCT_CREATED_QUEUE), eq(message));
    }

    @Test
    void sendProductStockUpdatedEvent_ShouldSendMessage() {
        ProductStockUpdatedMessage message = new ProductStockUpdatedMessage(
                UUID.randomUUID(), "P1", "Product", 100, 90, -10,
                "Reason", Instant.now());

        jmsProducerService.sendProductStockUpdatedEvent(message);

        verify(jmsTemplate).convertAndSend(eq(JmsConfig.PRODUCT_STOCK_UPDATED_QUEUE), eq(message));
    }
}
