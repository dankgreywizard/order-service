package com.skmcore.orderservice.event;

import java.util.UUID;

public record ProductStockUpdatedEvent(
        UUID productId,
        String productCode,
        int previousStockQuantity,
        int newStockQuantity,
        int stockChange,
        String reason
) {}
