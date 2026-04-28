package com.skmcore.orderservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductRequest(
    @NotNull(message = "Product code is mandatory")
    String productCode,

    @NotNull(message = "Product name is mandatory")
    String name,

    String description,

    @NotNull(message = "Price is mandatory")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    BigDecimal price,

    String category,

    @NotNull(message = "Stock quantity is mandatory")
    Integer stockQuantity,

    Boolean available
) {}
