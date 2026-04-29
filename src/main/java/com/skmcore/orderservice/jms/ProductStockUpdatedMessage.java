package com.skmcore.orderservice.jms;

import java.time.Instant;
import java.util.UUID;

public record ProductStockUpdatedMessage(
        UUID productId,
        String productCode,
        String productName,
        int previousStockQuantity,
        int newStockQuantity,
        int stockChange,
        String reason,
        Instant updatedAt
) {}
