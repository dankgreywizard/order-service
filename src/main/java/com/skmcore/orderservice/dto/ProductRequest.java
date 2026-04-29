package com.skmcore.orderservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record ProductRequest(
    @NotNull(message = "Product code is mandatory")
    @Size(min = 3, max = 50, message = "Product code must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Product code contains invalid characters")
    String productCode,

    @NotNull(message = "Product name is mandatory")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    String name,

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    String description,

    @NotNull(message = "Price is mandatory")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    BigDecimal price,

    @Size(max = 50, message = "Category cannot exceed 50 characters")
    String category,

    @NotNull(message = "Stock quantity is mandatory")
    Integer stockQuantity,

    Boolean available
) {}
