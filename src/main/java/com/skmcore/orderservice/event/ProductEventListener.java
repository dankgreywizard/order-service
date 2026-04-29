package com.skmcore.orderservice.event;

import com.skmcore.orderservice.jms.JmsProducerService;
import com.skmcore.orderservice.jms.ProductCreatedMessage;
import com.skmcore.orderservice.jms.ProductStockUpdatedMessage;
import com.skmcore.orderservice.model.Product;
import com.skmcore.orderservice.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
public class ProductEventListener {

    private final JmsProducerService jmsProducerService;
    private final ProductRepository productRepository;

    public ProductEventListener(JmsProducerService jmsProducerService,
                                ProductRepository productRepository) {
        this.jmsProducerService = jmsProducerService;
        this.productRepository = productRepository;
    }

    @EventListener
    @Async
    public void handleProductCreatedEvent(ProductCreatedEvent event) {
        log.info("Received ProductCreatedEvent for product ID: {}, code: {}",
                event.productId(), event.productCode());

        Product product = productRepository.findById(event.productId())
                .orElse(null);

        if (product != null) {
            ProductCreatedMessage message = new ProductCreatedMessage(
                    product.getId(),
                    product.getProductCode(),
                    product.getName(),
                    product.getDescription(),
                    product.getCategory(),
                    product.getPrice(),
                    product.getStockQuantity(),
                    product.getAvailable(),
                    Instant.now()
            );

            jmsProducerService.sendProductCreatedEvent(message);
        }
    }

    @EventListener
    @Async
    public void handleProductStockUpdatedEvent(ProductStockUpdatedEvent event) {
        log.info("Received ProductStockUpdatedEvent for product ID: {}. Stock changed from {} to {} (reason: {})",
                event.productId(), event.previousStockQuantity(), event.newStockQuantity(), event.reason());

        String productName = productRepository.findById(event.productId())
                .map(Product::getName)
                .orElse(event.productCode());

        ProductStockUpdatedMessage message = new ProductStockUpdatedMessage(
                event.productId(),
                event.productCode(),
                productName,
                event.previousStockQuantity(),
                event.newStockQuantity(),
                event.stockChange(),
                event.reason(),
                Instant.now()
        );

        jmsProducerService.sendProductStockUpdatedEvent(message);
    }
}
