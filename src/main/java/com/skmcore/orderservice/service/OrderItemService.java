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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderItemService {

    private static final Logger logger = LoggerFactory.getLogger(OrderItemService.class);

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderItemMapper orderItemMapper;

    public OrderItemService(OrderItemRepository orderItemRepository,
                            OrderRepository orderRepository,
                            ProductRepository productRepository,
                            OrderItemMapper orderItemMapper) {
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.orderItemMapper = orderItemMapper;
    }

    @Transactional(readOnly = true)
    public List<OrderItemResponse> getItemsByOrderId(UUID orderId) {
        return orderItemRepository.findByOrderId(orderId).stream()
                .map(orderItemMapper::toResponse)
                .collect(Collectors.toList());
    }

    public OrderItemResponse addItemToOrder(UUID orderId, AddItemRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.productId()));

        if (!product.getAvailable()) {
            throw new IllegalStateException("Product is not available for purchase: " + product.getName());
        }

        if (product.getStockQuantity() < request.quantity()) {
            throw new IllegalStateException(
                    String.format("Insufficient stock for product %s. Available: %d, Requested: %d",
                            product.getName(), product.getStockQuantity(), request.quantity()));
        }

        OrderItem orderItem = OrderItem.builder()
                .orderId(orderId)
                .productId(product.getId())
                .productName(product.getName())
                .quantity(request.quantity())
                .unitPrice(product.getPrice())
                .build();

        OrderItem saved = orderItemRepository.save(orderItem);

        product.setStockQuantity(product.getStockQuantity() - request.quantity());
        productRepository.save(product);

        updateOrderTotal(order);

        logger.info("Added item to order: orderId={}, productId={}, quantity={}",
                orderId, request.productId(), request.quantity());

        return orderItemMapper.toResponse(saved);
    }

    public void removeItemFromOrder(UUID orderId, UUID itemId) {
        OrderItem orderItem = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found with id: " + itemId));

        if (!orderItem.getOrderId().equals(orderId)) {
            throw new IllegalStateException("Order item does not belong to the specified order");
        }

        OrderItem orderItemToDelete = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found with id: " + itemId));

        UUID productId = orderItemToDelete.getProductId();
        Integer quantity = orderItemToDelete.getQuantity();

        orderItemRepository.deleteById(itemId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        product.setStockQuantity(product.getStockQuantity() + quantity);
        productRepository.save(product);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        updateOrderTotal(order);

        logger.info("Removed item from order: orderId={}, itemId={}", orderId, itemId);
    }

    private void updateOrderTotal(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        BigDecimal total = items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(total);
        orderRepository.save(order);
    }
}
