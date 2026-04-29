package com.skmcore.orderservice.jms;

import com.skmcore.orderservice.model.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderCreatedMessage(
        UUID orderId,
        String orderNumber,
        UUID customerId,
        String currency,
        BigDecimal totalAmount,
        Order.OrderStatus status,
        int itemCount,
        Instant createdAt
) {}
