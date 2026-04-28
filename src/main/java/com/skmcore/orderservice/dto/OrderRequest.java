package com.skmcore.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderRequest(
    @NotBlank(message = "Customer ID is mandatory")
    String customerId,

    @NotBlank(message = "Currency is mandatory")
    String currency
) {}
