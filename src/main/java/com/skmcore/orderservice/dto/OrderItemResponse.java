package com.skmcore.orderservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderItemResponse(
    UUID id,
    UUID productId,
    String productName,
    Integer quantity,
    BigDecimal unitPrice,
    LocalDateTime createdAt
) {}
