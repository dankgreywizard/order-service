package com.skmcore.orderservice.jms;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductCreatedMessage(
        UUID productId,
        String productCode,
        String productName,
        String description,
        String category,
        BigDecimal price,
        int stockQuantity,
        boolean available,
        Instant createdAt
) {}
