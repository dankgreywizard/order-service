package com.skmcore.orderservice.repository;

import com.skmcore.orderservice.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void saveAndFind_ShouldReturnProduct() {
        Product product = Product.builder()
            .productCode("PROD-001")
            .name("Test Product")
            .description("A test product")
            .price(new BigDecimal("29.99"))
            .category("Electronics")
            .stockQuantity(100)
            .available(true)
            .build();
        Product savedProduct = productRepository.save(product);

        Optional<Product> found = productRepository.findById(savedProduct.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Product");
        assertThat(found.get().getProductCode()).isEqualTo("PROD-001");
    }

    @Test
    void findByProductCode_ShouldReturnProduct() {
        Product product = Product.builder()
            .productCode("PROD-002")
            .name("Product By Code")
            .description("Test description")
            .price(new BigDecimal("49.99"))
            .category("Books")
            .stockQuantity(50)
            .available(true)
            .build();
        Product savedProduct = productRepository.save(product);

        Optional<Product> found = productRepository.findByProductCode("PROD-002");

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(savedProduct.getId());
        assertThat(found.get().getName()).isEqualTo("Product By Code");
    }

    @Test
    void findByAvailableTrue_ShouldReturnOnlyAvailableProducts() {
        Product availableProduct = Product.builder()
            .productCode("PROD-AVAIL-001")
            .name("Available Product")
            .description("Available")
            .price(new BigDecimal("19.99"))
            .category("Toys")
            .stockQuantity(25)
            .available(true)
            .build();

        Product unavailableProduct = Product.builder()
            .productCode("PROD-UNAVAIL-001")
            .name("Unavailable Product")
            .description("Not available")
            .price(new BigDecimal("39.99"))
            .category("Toys")
            .stockQuantity(0)
            .available(false)
            .build();

        productRepository.save(availableProduct);
        productRepository.save(unavailableProduct);

        List<Product> availableProducts = productRepository.findByAvailableTrue();

        assertThat(availableProducts).hasSize(1);
        assertThat(availableProducts.getFirst().getName()).isEqualTo("Available Product");
    }

    @Test
    void findByCategory_ShouldReturnProductsInCategory() {
        Product electronicsProduct1 = Product.builder()
            .productCode("PROD-ELEC-001")
            .name("Laptop")
            .description("A laptop")
            .price(new BigDecimal("999.99"))
            .category("Electronics")
            .stockQuantity(10)
            .available(true)
            .build();

        Product electronicsProduct2 = Product.builder()
            .productCode("PROD-ELEC-002")
            .name("Mouse")
            .description("A mouse")
            .price(new BigDecimal("29.99"))
            .category("Electronics")
            .stockQuantity(50)
            .available(true)
            .build();

        Product bookProduct = Product.builder()
            .productCode("PROD-BOOK-001")
            .name("Java Book")
            .description("Learn Java")
            .price(new BigDecimal("49.99"))
            .category("Books")
            .stockQuantity(30)
            .available(true)
            .build();

        productRepository.save(electronicsProduct1);
        productRepository.save(electronicsProduct2);
        productRepository.save(bookProduct);

        List<Product> electronics = productRepository.findByCategory("Electronics");

        assertThat(electronics).hasSize(2);
        assertThat(electronics).extracting("category").containsOnly("Electronics");
    }

    @Test
    void findByAvailableTrueAndCategory_ShouldReturnAvailableProductsInCategory() {
        Product availableElectronics = Product.builder()
            .productCode("PROD-AVAIL-ELEC-001")
            .name("Available Electronics")
            .description("Available electronics")
            .price(new BigDecimal("199.99"))
            .category("Electronics")
            .stockQuantity(20)
            .available(true)
            .build();

        Product unavailableElectronics = Product.builder()
            .productCode("PROD-UNAVAIL-ELEC-001")
            .name("Unavailable Electronics")
            .description("Unavailable electronics")
            .price(new BigDecimal("299.99"))
            .category("Electronics")
            .stockQuantity(0)
            .available(false)
            .build();

        productRepository.save(availableElectronics);
        productRepository.save(unavailableElectronics);

        List<Product> availableInCategory = productRepository.findByAvailableTrueAndCategory("Electronics");

        assertThat(availableInCategory).hasSize(1);
        assertThat(availableInCategory.getFirst().getName()).isEqualTo("Available Electronics");
    }

    @Test
    void existsById_ShouldReturnTrueWhenExists() {
        Product product = Product.builder()
            .productCode("PROD-EXISTS-001")
            .name("Exists Product")
            .description("Test")
            .price(new BigDecimal("9.99"))
            .category("Test")
            .stockQuantity(5)
            .available(true)
            .build();
        Product savedProduct = productRepository.save(product);

        boolean exists = productRepository.existsById(savedProduct.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void deleteById_ShouldDeleteProduct() {
        Product product = Product.builder()
            .productCode("PROD-DELETE-001")
            .name("Delete Product")
            .description("To be deleted")
            .price(new BigDecimal("14.99"))
            .category("Test")
            .stockQuantity(10)
            .available(true)
            .build();
        Product savedProduct = productRepository.save(product);

        productRepository.deleteById(savedProduct.getId());

        Optional<Product> found = productRepository.findById(savedProduct.getId());
        assertThat(found).isEmpty();
    }
}
