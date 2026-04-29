package com.skmcore.orderservice.jms;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderItemRemovedMessage(
        UUID orderId,
        String orderNumber,
        UUID itemId,
        UUID productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalItemPrice,
        Instant removedAt,
        String reason
) {}
