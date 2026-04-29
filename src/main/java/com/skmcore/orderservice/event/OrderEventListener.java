package com.skmcore.orderservice.event;

import com.skmcore.orderservice.jms.*;
import com.skmcore.orderservice.model.Order;
import com.skmcore.orderservice.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
public class OrderEventListener {

    private final JmsProducerService jmsProducerService;
    private final OrderRepository orderRepository;

    public OrderEventListener(JmsProducerService jmsProducerService, OrderRepository orderRepository) {
        this.jmsProducerService = jmsProducerService;
        this.orderRepository = orderRepository;
    }

    @EventListener
    @Async
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent for order ID: {}", event.orderId());

        Order order = event.order();
        OrderCreatedMessage message = new OrderCreatedMessage(
                order.getId(),
                order.getOrderNumber(),
                UUID.fromString(order.getCustomerId()),
                order.getCurrency(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getItems() != null ? order.getItems().size() : 0,
                Instant.now()
        );

        jmsProducerService.sendOrderCreatedEvent(message);
    }

    @EventListener
    @Async
    public void handleOrderStatusChangedEvent(OrderStatusChangedEvent event) {
        log.info("Received OrderStatusChangedEvent for order ID: {}. Status changed from {} to {}",
                event.orderId(), event.oldStatus(), event.newStatus());

        String orderNumber = orderRepository.findById(event.orderId())
                .map(Order::getOrderNumber)
                .orElse(event.orderId().toString());

        OrderStatusChangedMessage message = new OrderStatusChangedMessage(
                event.orderId(),
                orderNumber,
                event.oldStatus(),
                event.newStatus(),
                Instant.now()
        );

        jmsProducerService.sendOrderStatusChangedEvent(message);
    }

    @EventListener
    @Async
    public void handleOrderItemAddedEvent(OrderItemAddedEvent event) {
        log.info("Received OrderItemAddedEvent for order ID: {}, item ID: {}", event.orderId(), event.itemId());

        String orderNumber = orderRepository.findById(event.orderId())
                .map(Order::getOrderNumber)
                .orElse(event.orderId().toString());

        OrderItemAddedMessage message = new OrderItemAddedMessage(
                event.orderId(),
                orderNumber,
                event.itemId(),
                event.productId(),
                event.productName(),
                event.quantity(),
                event.unitPrice(),
                event.unitPrice().multiply(java.math.BigDecimal.valueOf(event.quantity())),
                Instant.now()
        );

        jmsProducerService.sendOrderItemAddedEvent(message);
    }

    @EventListener
    @Async
    public void handleOrderItemRemovedEvent(OrderItemRemovedEvent event) {
        log.info("Received OrderItemRemovedEvent for order ID: {}, item ID: {}", event.orderId(), event.itemId());

        String orderNumber = orderRepository.findById(event.orderId())
                .map(Order::getOrderNumber)
                .orElse(event.orderId().toString());

        OrderItemRemovedMessage message = new OrderItemRemovedMessage(
                event.orderId(),
                orderNumber,
                event.itemId(),
                event.productId(),
                event.productName(),
                event.quantity(),
                event.unitPrice(),
                event.unitPrice().multiply(java.math.BigDecimal.valueOf(event.quantity())),
                Instant.now(),
                event.reason()
        );

        jmsProducerService.sendOrderItemRemovedEvent(message);
    }
}
