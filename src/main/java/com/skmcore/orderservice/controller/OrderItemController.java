package com.skmcore.orderservice.controller;

import com.skmcore.orderservice.dto.AddItemRequest;
import com.skmcore.orderservice.dto.OrderItemResponse;
import com.skmcore.orderservice.service.OrderItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Order Items", description = "Order item management APIs")
public class OrderItemController {

    private final OrderItemService orderItemService;

    public OrderItemController(OrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }

    @GetMapping("/{orderId}/items")
    @Operation(summary = "Get order items", description = "Returns all items in a specific order")
    public ResponseEntity<List<OrderItemResponse>> getItemsByOrderId(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderItemService.getItemsByOrderId(orderId));
    }

    @PostMapping("/{orderId}/items")
    @Operation(summary = "Add item to order", description = "Adds a product item to an existing order")
    public ResponseEntity<OrderItemResponse> addItemToOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody AddItemRequest request) {
        OrderItemResponse response = orderItemService.addItemToOrder(orderId, request);
        return ResponseEntity.created(URI.create("/api/v1/orders/" + orderId + "/items/" + response.id()))
                .body(response);
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    @Operation(summary = "Remove item from order", description = "Removes an item from an order and restores product stock")
    public ResponseEntity<Void> removeItemFromOrder(
            @PathVariable UUID orderId,
            @PathVariable UUID itemId) {
        orderItemService.removeItemFromOrder(orderId, itemId);
        return ResponseEntity.noContent().build();
    }
}
