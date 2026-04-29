package com.skmcore.orderservice.event;

import com.skmcore.orderservice.jms.*;
import com.skmcore.orderservice.model.Product;
import com.skmcore.orderservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductEventListenerTest {

    @Mock
    private JmsProducerService jmsProducerService;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductEventListener productEventListener;

    private UUID productId;
    private Product product;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        product = Product.builder()
                .id(productId)
                .productCode("P123")
                .name("Test Product")
                .price(BigDecimal.TEN)
                .stockQuantity(100)
                .available(true)
                .build();
    }

    @Test
    void handleProductCreatedEvent_ShouldSendMessage() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        ProductCreatedEvent event = new ProductCreatedEvent(productId, "P123", "Test Product");

        productEventListener.handleProductCreatedEvent(event);

        verify(jmsProducerService).sendProductCreatedEvent(any(ProductCreatedMessage.class));
    }

    @Test
    void handleProductStockUpdatedEvent_ShouldSendMessage() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        ProductStockUpdatedEvent event = new ProductStockUpdatedEvent(productId, "P123", 100, 90, -10, "Update");

        productEventListener.handleProductStockUpdatedEvent(event);

        verify(jmsProducerService).sendProductStockUpdatedEvent(any(ProductStockUpdatedMessage.class));
    }
}
