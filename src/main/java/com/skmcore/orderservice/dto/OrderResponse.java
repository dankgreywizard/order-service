package com.skmcore.orderservice.dto;

import com.skmcore.orderservice.model.Order.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
    UUID id,
    String orderNumber,
    String customerId,
    String currency,
    BigDecimal totalAmount,
    OrderStatus status,
    List<OrderItemResponse> items,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
