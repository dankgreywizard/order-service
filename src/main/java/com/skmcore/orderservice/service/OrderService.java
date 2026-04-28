package com.skmcore.orderservice.service;

import com.skmcore.orderservice.dto.OrderItemResponse;
import com.skmcore.orderservice.dto.OrderRequest;
import com.skmcore.orderservice.dto.OrderResponse;
import com.skmcore.orderservice.event.OrderCreatedEvent;
import com.skmcore.orderservice.event.OrderStatusChangedEvent;
import com.skmcore.orderservice.exception.ResourceNotFoundException;
import com.skmcore.orderservice.mapper.OrderItemMapper;
import com.skmcore.orderservice.mapper.OrderMapper;
import com.skmcore.orderservice.model.Order;
import com.skmcore.orderservice.repository.OrderItemRepository;
import com.skmcore.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        log.info("Creating order for customer: {}", request.customerId());
        Order order = orderMapper.toEntity(request);
        order.setOrderNumber(orderMapper.generateOrderNumber());
        Order savedOrder = orderRepository.save(order);
        log.info("Order created with ID: {}", savedOrder.getId());

        eventPublisher.publishEvent(new OrderCreatedEvent(savedOrder.getId(), savedOrder));

        return mapToResponse(savedOrder);
    }

    @Transactional
    public OrderResponse updateOrderStatus(UUID id, Order.OrderStatus newStatus) {
        log.info("Updating order status for ID: {} to {}", id, newStatus);
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));

        Order.OrderStatus oldStatus = order.getStatus();
        if (oldStatus != newStatus) {
            order.setStatus(newStatus);
            Order updatedOrder = orderRepository.save(order);
            log.info("Order status updated for ID: {} from {} to {}", id, oldStatus, newStatus);

            eventPublisher.publishEvent(new OrderStatusChangedEvent(id, oldStatus, newStatus));

            return mapToResponse(updatedOrder);
        }

        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID id) {
        log.debug("Fetching order by ID: {}", id);
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        log.debug("Fetching all orders");
        return orderRepository.findAll().stream()
            .map(this::mapToResponse)
            .toList();
    }

    @Transactional
    public void deleteOrder(UUID id) {
        log.info("Deleting order with ID: {}", id);
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Order not found with ID: " + id);
        }
        orderRepository.deleteById(id);
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> items = orderItemRepository.findByOrderId(order.getId()).stream()
                .map(orderItemMapper::toResponse)
                .collect(Collectors.toList());
        return orderMapper.toResponse(order, items);
    }
}
