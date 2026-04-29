package com.skmcore.orderservice.service;

import com.skmcore.orderservice.dto.AddItemRequest;
import com.skmcore.orderservice.dto.OrderItemResponse;
import com.skmcore.orderservice.exception.ResourceNotFoundException;
import com.skmcore.orderservice.mapper.OrderItemMapper;
import com.skmcore.orderservice.model.Order;
import com.skmcore.orderservice.model.OrderItem;
import com.skmcore.orderservice.model.Product;
import com.skmcore.orderservice.repository.OrderItemRepository;
import com.skmcore.orderservice.repository.OrderRepository;
import com.skmcore.orderservice.repository.ProductRepository;
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
class OrderItemServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderItemService orderItemService;

    private UUID orderId;
    private UUID productId;
    private UUID itemId;
    private Order order;
    private Product product;
    private OrderItem orderItem;
    private AddItemRequest addItemRequest;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        productId = UUID.randomUUID();
        itemId = UUID.randomUUID();

        order = Order.builder()
            .id(orderId)
            .orderNumber("ORD-12345")
            .customerId("customer-123")
            .currency("USD")
            .totalAmount(new BigDecimal("100.00"))
            .status(Order.OrderStatus.PENDING)
            .build();

        product = Product.builder()
            .id(productId)
            .productCode("PROD-001")
            .name("Test Product")
            .description("A test product")
            .price(new BigDecimal("25.00"))
            .category("Electronics")
            .stockQuantity(100)
            .available(true)
            .build();

        orderItem = OrderItem.builder()
            .id(itemId)
            .orderId(orderId)
            .productId(productId)
            .productName("Test Product")
            .quantity(2)
            .unitPrice(new BigDecimal("25.00"))
            .build();

        addItemRequest = new AddItemRequest(productId, 2);
    }

    @Test
    void getItemsByOrderId_ShouldReturnOrderItems() {
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(List.of(orderItem));
        OrderItemResponse response = new OrderItemResponse(itemId, productId, "Test Product", 2, new BigDecimal("25.00"), LocalDateTime.now());
        when(orderItemMapper.toResponse(orderItem)).thenReturn(response);

        List<OrderItemResponse> result = orderItemService.getItemsByOrderId(orderId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().productName()).isEqualTo("Test Product");
        verify(orderItemRepository).findByOrderId(orderId);
    }

    @Test
    void getItemsByOrderId_WhenNoItems_ShouldReturnEmptyList() {
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(List.of());

        List<OrderItemResponse> result = orderItemService.getItemsByOrderId(orderId);

        assertThat(result).isEmpty();
    }

    @Test
    void addItemToOrder_ShouldAddItemAndUpdateStock() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(orderItem);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(List.of(orderItem));
        OrderItemResponse response = new OrderItemResponse(itemId, productId, "Test Product", 2, new BigDecimal("25.00"), LocalDateTime.now());
        when(orderItemMapper.toResponse(orderItem)).thenReturn(response);

        OrderItemResponse result = orderItemService.addItemToOrder(orderId, addItemRequest);

        assertThat(result).isNotNull();
        assertThat(result.productId()).isEqualTo(productId);
        verify(orderItemRepository).save(any(OrderItem.class));
        verify(productRepository).save(product);
        verify(eventPublisher).publishEvent(any(com.skmcore.orderservice.event.OrderItemAddedEvent.class));
        assertThat(product.getStockQuantity()).isEqualTo(98);
    }

    @Test
    void addItemToOrder_WhenOrderNotFound_ShouldThrowException() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderItemService.addItemToOrder(orderId, addItemRequest))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Order not found");
    }

    @Test
    void addItemToOrder_WhenProductNotFound_ShouldThrowException() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderItemService.addItemToOrder(orderId, addItemRequest))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Product not found");
    }

    @Test
    void addItemToOrder_WhenProductNotAvailable_ShouldThrowException() {
        product.setAvailable(false);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderItemService.addItemToOrder(orderId, addItemRequest))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Product is not available for purchase");
    }

    @Test
    void addItemToOrder_WhenInsufficientStock_ShouldThrowException() {
        product.setStockQuantity(1);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderItemService.addItemToOrder(orderId, addItemRequest))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Insufficient stock");
    }

    @Test
    void removeItemFromOrder_ShouldRemoveItemAndRestoreStock() {
        when(orderItemRepository.findById(itemId)).thenReturn(Optional.of(orderItem));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(List.of());
        doNothing().when(orderItemRepository).deleteById(itemId);

        orderItemService.removeItemFromOrder(orderId, itemId);

        verify(orderItemRepository).deleteById(itemId);
        verify(productRepository).save(product);
        verify(eventPublisher).publishEvent(any(com.skmcore.orderservice.event.OrderItemRemovedEvent.class));
        verify(eventPublisher).publishEvent(any(com.skmcore.orderservice.event.ProductStockUpdatedEvent.class));
        assertThat(product.getStockQuantity()).isEqualTo(102);
    }

    @Test
    void removeItemFromOrder_WhenOrderItemNotFound_ShouldThrowException() {
        when(orderItemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderItemService.removeItemFromOrder(orderId, itemId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Order item not found");
    }

    @Test
    void removeItemFromOrder_WhenOrderItemDoesNotBelongToOrder_ShouldThrowException() {
        UUID differentOrderId = UUID.randomUUID();
        orderItem.setOrderId(differentOrderId);
        when(orderItemRepository.findById(itemId)).thenReturn(Optional.of(orderItem));

        assertThatThrownBy(() -> orderItemService.removeItemFromOrder(orderId, itemId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Order item does not belong to the specified order");
    }

    @Test
    void removeItemFromOrder_WhenProductNotFound_ShouldThrowException() {
        when(orderItemRepository.findById(itemId)).thenReturn(Optional.of(orderItem));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderItemService.removeItemFromOrder(orderId, itemId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void removeItemFromOrder_WhenOrderNotFound_ShouldThrowException() {
        when(orderItemRepository.findById(itemId)).thenReturn(Optional.of(orderItem));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderItemService.removeItemFromOrder(orderId, itemId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order not found");
    }
}
