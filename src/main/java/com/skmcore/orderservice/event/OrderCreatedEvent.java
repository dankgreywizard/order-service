package com.skmcore.orderservice.event;

import com.skmcore.orderservice.model.Order;
import java.util.UUID;

public record OrderCreatedEvent(UUID orderId, Order order) {}
