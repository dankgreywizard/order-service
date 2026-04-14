package com.skmcore.orderservice.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderEventListener {

    @EventListener
    @Async
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent for order ID: {}", event.orderId());
        // Add business logic here (e.g., send email, notify other services)
    }

    @EventListener
    @Async
    public void handleOrderStatusChangedEvent(OrderStatusChangedEvent event) {
        log.info("Received OrderStatusChangedEvent for order ID: {}. Status changed from {} to {}", 
            event.orderId(), event.oldStatus(), event.newStatus());
        // Add business logic here (e.g., trigger shipping if status is SHIPPED)
    }
}
