package com.skmcore.orderservice.mapper;

import com.skmcore.orderservice.dto.ProductRequest;
import com.skmcore.orderservice.dto.ProductResponse;
import com.skmcore.orderservice.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ProductMapperImpl.class)
class ProductMapperTest {

    @Autowired
    private ProductMapper productMapper;

    @Test
    void toEntity_ShouldMapProductRequestToProduct() {
        ProductRequest request = new ProductRequest(
            "PROD-001",
            "Test Product",
            "A test product description",
            new BigDecimal("29.99"),
            "Electronics",
            100,
            true
        );

        Product product = productMapper.toEntity(request);

        assertThat(product).isNotNull();
        assertThat(product.getProductCode()).isEqualTo("PROD-001");
        assertThat(product.getName()).isEqualTo("Test Product");
        assertThat(product.getDescription()).isEqualTo("A test product description");
        assertThat(product.getPrice()).isEqualByComparingTo(new BigDecimal("29.99"));
        assertThat(product.getCategory()).isEqualTo("Electronics");
        assertThat(product.getStockQuantity()).isEqualTo(100);
        assertThat(product.getAvailable()).isTrue();
        assertThat(product.getId()).isNull();
        assertThat(product.getCreatedAt()).isNull();
        assertThat(product.getUpdatedAt()).isNull();
    }

    @Test
    void toEntity_WithNullAvailable_ShouldSetAvailableToTrue() {
        ProductRequest request = new ProductRequest(
            "PROD-002",
            "Another Product",
            "Description",
            new BigDecimal("19.99"),
            "Books",
            50,
            null
        );

        Product product = productMapper.toEntity(request);

        assertThat(product).isNotNull();
        assertThat(product.getAvailable()).isTrue();
    }

    @Test
    void toResponse_ShouldMapProductToProductResponse() {
        Product product = Product.builder()
            .id(java.util.UUID.randomUUID())
            .productCode("PROD-003")
            .name("Response Product")
            .description("Response description")
            .price(new BigDecimal("39.99"))
            .category("Toys")
            .stockQuantity(25)
            .available(true)
            .build();

        ProductResponse response = productMapper.toResponse(product);

        assertThat(response).isNotNull();
        assertThat(response.productCode()).isEqualTo("PROD-003");
        assertThat(response.name()).isEqualTo("Response Product");
        assertThat(response.description()).isEqualTo("Response description");
        assertThat(response.price()).isEqualByComparingTo(new BigDecimal("39.99"));
        assertThat(response.category()).isEqualTo("Toys");
        assertThat(response.stockQuantity()).isEqualTo(25);
        assertThat(response.available()).isTrue();
    }
}
