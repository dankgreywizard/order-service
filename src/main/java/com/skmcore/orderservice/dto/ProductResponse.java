package com.skmcore.orderservice.dto;

import com.skmcore.orderservice.model.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductResponse(
    UUID id,
    String productCode,
    String name,
    String description,
    BigDecimal price,
    String category,
    Integer stockQuantity,
    Boolean available,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ProductResponse fromEntity(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getProductCode(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getCategory(),
            product.getStockQuantity(),
            product.getAvailable(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
    }
}
