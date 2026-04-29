package com.skmcore.orderservice.service;

import com.skmcore.orderservice.dto.ProductRequest;
import com.skmcore.orderservice.dto.ProductResponse;
import com.skmcore.orderservice.exception.ResourceNotFoundException;
import com.skmcore.orderservice.mapper.ProductMapper;
import com.skmcore.orderservice.model.Product;
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
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ProductService productService;

    private UUID productId;
    private Product product;
    private ProductRequest productRequest;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        productRequest = new ProductRequest(
            "PROD-001",
            "Test Product",
            "A test product",
            new BigDecimal("29.99"),
            "Electronics",
            100,
            true
        );
        product = Product.builder()
            .id(productId)
            .productCode("PROD-001")
            .name("Test Product")
            .description("A test product")
            .price(new BigDecimal("29.99"))
            .category("Electronics")
            .stockQuantity(100)
            .available(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        productResponse = new ProductResponse(
            productId,
            "PROD-001",
            "Test Product",
            "A test product",
            new BigDecimal("29.99"),
            "Electronics",
            100,
            true,
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
    }

    @Test
    void getAllProducts_ShouldReturnListOfProducts() {
        when(productRepository.findAll()).thenReturn(List.of(product));
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        List<ProductResponse> result = productService.getAllProducts();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("Test Product");
    }

    @Test
    void getAvailableProducts_ShouldReturnOnlyAvailableProducts() {
        Product unavailableProduct = Product.builder()
            .id(UUID.randomUUID())
            .productCode("PROD-002")
            .name("Unavailable Product")
            .available(false)
            .price(new BigDecimal("19.99"))
            .stockQuantity(0)
            .build();

        when(productRepository.findByAvailableTrue()).thenReturn(List.of(product));
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        List<ProductResponse> result = productService.getAvailableProducts();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().available()).isTrue();
    }

    @Test
    void getProductById_WhenProductExists_ShouldReturnProduct() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        ProductResponse result = productService.getProductById(productId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(productId);
    }

    @Test
    void getProductById_WhenProductDoesNotExist_ShouldThrowException() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(productId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Product not found with id");
    }

    @Test
    void getProductByCode_WhenProductExists_ShouldReturnProduct() {
        when(productRepository.findByProductCode("PROD-001")).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        ProductResponse result = productService.getProductByCode("PROD-001");

        assertThat(result).isNotNull();
        assertThat(result.productCode()).isEqualTo("PROD-001");
    }

    @Test
    void getProductByCode_WhenProductDoesNotExist_ShouldThrowException() {
        when(productRepository.findByProductCode("PROD-001")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductByCode("PROD-001"))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Product not found with code");
    }

    @Test
    void getProductsByCategory_ShouldReturnProductsInCategory() {
        when(productRepository.findByCategory("Electronics")).thenReturn(List.of(product));
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        List<ProductResponse> result = productService.getProductsByCategory("Electronics");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().category()).isEqualTo("Electronics");
    }

    @Test
    void getAvailableProductsByCategory_ShouldReturnAvailableProductsInCategory() {
        when(productRepository.findByAvailableTrueAndCategory("Electronics")).thenReturn(List.of(product));
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        List<ProductResponse> result = productService.getAvailableProductsByCategory("Electronics");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().available()).isTrue();
        assertThat(result.getFirst().category()).isEqualTo("Electronics");
    }

    @Test
    void createProduct_ShouldReturnCreatedProduct() {
        when(productMapper.toEntity(productRequest)).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        ProductResponse result = productService.createProduct(productRequest);

        assertThat(result).isNotNull();
        assertThat(result.productCode()).isEqualTo("PROD-001");
        verify(productRepository).save(any(Product.class));
        verify(eventPublisher).publishEvent(any(com.skmcore.orderservice.event.ProductCreatedEvent.class));
    }

    @Test
    void updateProduct_WhenProductExists_ShouldReturnUpdatedProduct() {
        ProductRequest updateRequest = new ProductRequest(
            "PROD-001",
            "Updated Product",
            "Updated description",
            new BigDecimal("39.99"),
            "Electronics",
            150,
            true
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        ProductResponse result = productService.updateProduct(productId, updateRequest);

        assertThat(result).isNotNull();
        verify(productRepository).save(product);
        verify(eventPublisher).publishEvent(any(com.skmcore.orderservice.event.ProductStockUpdatedEvent.class));
        assertThat(product.getName()).isEqualTo("Updated Product");
        assertThat(product.getDescription()).isEqualTo("Updated description");
        assertThat(product.getPrice()).isEqualByComparingTo(new BigDecimal("39.99"));
        assertThat(product.getStockQuantity()).isEqualTo(150);
    }

    @Test
    void updateProduct_WhenProductDoesNotExist_ShouldThrowException() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(productId, productRequest))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Product not found with id");
    }

    @Test
    void updateProduct_WithNullAvailable_ShouldNotChangeAvailability() {
        ProductRequest updateRequest = new ProductRequest(
            "PROD-001",
            "Updated Product",
            "Updated description",
            new BigDecimal("39.99"),
            "Electronics",
            150,
            null
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        productService.updateProduct(productId, updateRequest);

        assertThat(product.getAvailable()).isTrue();
    }

    @Test
    void deleteProduct_WhenProductExists_ShouldDeleteProduct() {
        when(productRepository.existsById(productId)).thenReturn(true);

        productService.deleteProduct(productId);

        verify(productRepository).deleteById(productId);
    }

    @Test
    void deleteProduct_WhenProductDoesNotExist_ShouldThrowException() {
        when(productRepository.existsById(productId)).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProduct(productId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Product not found with id");
    }
}
