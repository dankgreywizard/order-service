package com.skmcore.orderservice.event;

import java.util.UUID;

public record ProductCreatedEvent(
        UUID productId,
        String productCode,
        String name
) {}
