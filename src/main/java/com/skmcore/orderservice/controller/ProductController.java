package com.skmcore.orderservice.controller;

import com.skmcore.orderservice.dto.ProductRequest;
import com.skmcore.orderservice.dto.ProductResponse;
import com.skmcore.orderservice.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Product catalog management APIs")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Get all products", description = "Returns a list of all products in the catalog")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/available")
    @Operation(summary = "Get available products", description = "Returns a list of products that are currently available for purchase")
    public ResponseEntity<List<ProductResponse>> getAvailableProducts() {
        return ResponseEntity.ok(productService.getAvailableProducts());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Returns a specific product by its UUID")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/code/{productCode}")
    @Operation(summary = "Get product by code", description = "Returns a specific product by its product code")
    public ResponseEntity<ProductResponse> getProductByCode(
            @PathVariable @NotBlank @Size(min = 3, max = 50) @Pattern(regexp = "^[A-Za-z0-9_-]+$") String productCode) {
        return ResponseEntity.ok(productService.getProductByCode(productCode));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get products by category", description = "Returns all products in a specific category")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(
            @PathVariable @NotBlank @Size(max = 50) String category) {
        return ResponseEntity.ok(productService.getProductsByCategory(category));
    }

    @GetMapping("/category/{category}/available")
    @Operation(summary = "Get available products by category", description = "Returns available products in a specific category")
    public ResponseEntity<List<ProductResponse>> getAvailableProductsByCategory(
            @PathVariable @NotBlank @Size(max = 50) String category) {
        return ResponseEntity.ok(productService.getAvailableProductsByCategory(category));
    }

    @PostMapping
    @Operation(summary = "Create product", description = "Creates a new product in the catalog")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.created(URI.create("/api/v1/products/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Updates an existing product")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Deletes a product from the catalog")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
