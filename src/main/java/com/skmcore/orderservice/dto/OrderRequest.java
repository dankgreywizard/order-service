package com.skmcore.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record OrderRequest(
    @NotBlank(message = "Customer ID is mandatory")
    @Size(min = 3, max = 50, message = "Customer ID must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Customer ID contains invalid characters")
    String customerId,

    @NotBlank(message = "Currency is mandatory")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be 3 uppercase letters")
    String currency
) {}
