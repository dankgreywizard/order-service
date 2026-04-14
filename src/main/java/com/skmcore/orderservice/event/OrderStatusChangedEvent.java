package com.skmcore.orderservice.event;

import com.skmcore.orderservice.model.Order;
import java.util.UUID;

public record OrderStatusChangedEvent(
    UUID orderId,
    Order.OrderStatus oldStatus,
    Order.OrderStatus newStatus
) {}
