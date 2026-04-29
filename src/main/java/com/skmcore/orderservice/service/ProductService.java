package com.skmcore.orderservice.service;

import com.skmcore.orderservice.dto.ProductRequest;
import com.skmcore.orderservice.dto.ProductResponse;
import com.skmcore.orderservice.event.ProductCreatedEvent;
import com.skmcore.orderservice.event.ProductStockUpdatedEvent;
import com.skmcore.orderservice.exception.ResourceNotFoundException;
import com.skmcore.orderservice.mapper.ProductMapper;
import com.skmcore.orderservice.model.Product;
import com.skmcore.orderservice.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ApplicationEventPublisher eventPublisher;

    public ProductService(ProductRepository productRepository,
                          ProductMapper productMapper,
                          ApplicationEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAvailableProducts() {
        return productRepository.findByAvailableTrue().stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {
        return productRepository.findById(id)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductByCode(String productCode) {
        return productRepository.findByProductCode(productCode)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with code: " + productCode));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByCategory(String category) {
        return productRepository.findByCategory(category).stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAvailableProductsByCategory(String category) {
        return productRepository.findByAvailableTrueAndCategory(category).stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse createProduct(ProductRequest request) {
        Product product = productMapper.toEntity(request);
        Product saved = productRepository.save(product);
        logger.info("Created product with id: {}", saved.getId());

        eventPublisher.publishEvent(new ProductCreatedEvent(
                saved.getId(), saved.getProductCode(), saved.getName()
        ));

        return productMapper.toResponse(saved);
    }

    public ProductResponse updateProduct(UUID id, ProductRequest request) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        int previousStock = existing.getStockQuantity();

        existing.setProductCode(request.productCode());
        existing.setName(request.name());
        existing.setDescription(request.description());
        existing.setPrice(request.price());
        existing.setCategory(request.category());
        existing.setStockQuantity(request.stockQuantity());

        if (request.available() != null) {
            existing.setAvailable(request.available());
        }

        Product updated = productRepository.save(existing);
        logger.info("Updated product with id: {}", updated.getId());

        int stockChange = request.stockQuantity() - previousStock;
        if (stockChange != 0) {
            eventPublisher.publishEvent(new ProductStockUpdatedEvent(
                    updated.getId(), updated.getProductCode(),
                    previousStock, updated.getStockQuantity(),
                    stockChange, "Product update"
            ));
        }

        return productMapper.toResponse(updated);
    }

    public void deleteProduct(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
        logger.info("Deleted product with id: {}", id);
    }
}
