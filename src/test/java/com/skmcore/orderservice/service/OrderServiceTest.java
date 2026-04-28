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
import com.skmcore.orderservice.model.OrderItem;
import com.skmcore.orderservice.repository.OrderItemRepository;
import com.skmcore.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    private Order order;
    private OrderRequest orderRequest;
    private OrderResponse orderResponse;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        orderRequest = new OrderRequest("customer-123", "USD");
        order = Order.builder()
            .id(orderId)
            .orderNumber("ORD-12345")
            .customerId("customer-123")
            .currency("USD")
            .totalAmount(new BigDecimal("100.00"))
            .status(Order.OrderStatus.PENDING)
            .build();
        orderResponse = new OrderResponse(orderId, "ORD-12345", "customer-123", "USD", new BigDecimal("100.00"), Order.OrderStatus.PENDING, List.of(), null, null);
    }

    @Test
    void createOrder_ShouldReturnOrderResponse() {
        when(orderMapper.toEntity(any(OrderRequest.class))).thenReturn(order);
        when(orderMapper.generateOrderNumber()).thenReturn("ORD-12345");
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.findByOrderId(order.getId())).thenReturn(List.of());
        when(orderMapper.toResponse(any(Order.class), any(List.class))).thenReturn(orderResponse);

        OrderResponse result = orderService.createOrder(orderRequest);

        assertThat(result).isNotNull();
        assertThat(result.customerId()).isEqualTo("customer-123");
        verify(orderRepository).save(any(Order.class));
        verify(eventPublisher).publishEvent(any(OrderCreatedEvent.class));
    }

    @Test
    void updateOrderStatus_WhenStatusChanges_ShouldPublishEvent() {
        Order.OrderStatus newStatus = Order.OrderStatus.SHIPPED;
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.findByOrderId(order.getId())).thenReturn(List.of());
        when(orderMapper.toResponse(any(Order.class), any(List.class))).thenReturn(orderResponse);

        orderService.updateOrderStatus(orderId, newStatus);

        verify(orderRepository).save(any(Order.class));
        verify(eventPublisher).publishEvent(any(OrderStatusChangedEvent.class));
    }

    @Test
    void updateOrderStatus_WhenStatusIsSame_ShouldNotPublishEvent() {
        Order.OrderStatus sameStatus = Order.OrderStatus.PENDING;
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(order.getId())).thenReturn(List.of());
        when(orderMapper.toResponse(any(Order.class), any(List.class))).thenReturn(orderResponse);

        orderService.updateOrderStatus(orderId, sameStatus);

        verify(orderRepository, never()).save(any(Order.class));
        verify(eventPublisher, never()).publishEvent(any(OrderStatusChangedEvent.class));
    }

    @Test
    void getOrderById_WhenOrderExists_ShouldReturnOrderResponse() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(order.getId())).thenReturn(List.of());
        when(orderMapper.toResponse(any(Order.class), any(List.class))).thenReturn(orderResponse);

        OrderResponse result = orderService.getOrderById(orderId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(orderId);
    }

    @Test
    void getOrderById_WhenOrderDoesNotExist_ShouldThrowException() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(orderId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Order not found");
    }

    @Test
    void deleteOrder_WhenOrderExists_ShouldDeleteOrder() {
        when(orderRepository.existsById(orderId)).thenReturn(true);

        orderService.deleteOrder(orderId);

        verify(orderRepository).deleteById(orderId);
    }

    @Test
    void deleteOrder_WhenOrderDoesNotExist_ShouldThrowException() {
        when(orderRepository.existsById(orderId)).thenReturn(false);

        assertThatThrownBy(() -> orderService.deleteOrder(orderId))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllOrders_ShouldReturnList() {
        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(orderItemRepository.findByOrderId(order.getId())).thenReturn(List.of());
        when(orderMapper.toResponse(any(Order.class), any(List.class))).thenReturn(orderResponse);

        List<OrderResponse> result = orderService.getAllOrders();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(orderResponse);
    }

    @Test
    void getOrderById_WithItems_ShouldReturnOrderResponseWithItems() {
        UUID itemId = UUID.randomUUID();
        OrderItem orderItem = OrderItem.builder()
            .id(itemId)
            .orderId(orderId)
            .productId(UUID.randomUUID())
            .productName("Test Product")
            .quantity(2)
            .unitPrice(new BigDecimal("50.00"))
            .build();
        OrderItemResponse itemResponse = new OrderItemResponse(itemId, orderItem.getProductId(),
            orderItem.getProductName(), orderItem.getQuantity(), orderItem.getUnitPrice(), LocalDateTime.now());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(List.of(orderItem));
        when(orderItemMapper.toResponse(orderItem)).thenReturn(itemResponse);
        when(orderMapper.toResponse(any(Order.class), any(List.class))).thenReturn(orderResponse);

        OrderResponse result = orderService.getOrderById(orderId);

        assertThat(result).isNotNull();
        verify(orderItemRepository).findByOrderId(orderId);
    }
}
