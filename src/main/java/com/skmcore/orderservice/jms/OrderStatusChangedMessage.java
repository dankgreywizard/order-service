package com.skmcore.orderservice.jms;

import com.skmcore.orderservice.model.Order;

import java.time.Instant;
import java.util.UUID;

public record OrderStatusChangedMessage(
        UUID orderId,
        String orderNumber,
        Order.OrderStatus oldStatus,
        Order.OrderStatus newStatus,
        Instant changedAt
) {}
