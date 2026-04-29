package com.skmcore.orderservice.event;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemAddedEvent(
        UUID orderId,
        UUID itemId,
        UUID productId,
        String productName,
        int quantity,
        BigDecimal unitPrice
) {}
