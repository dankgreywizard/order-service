package com.skmcore.orderservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record AddItemRequest(
    @NotNull(message = "Product ID is mandatory")
    UUID productId,

    @NotNull(message = "Quantity is mandatory")
    @Positive(message = "Quantity must be positive")
    Integer quantity
) {}
