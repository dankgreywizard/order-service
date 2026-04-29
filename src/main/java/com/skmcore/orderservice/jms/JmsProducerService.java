package com.skmcore.orderservice.jms;

import com.skmcore.orderservice.config.JmsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class JmsProducerService {

    private static final Logger logger = LoggerFactory.getLogger(JmsProducerService.class);

    private final JmsTemplate jmsTemplate;

    public JmsProducerService(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void sendOrderCreatedEvent(OrderCreatedMessage message) {
        logger.info("Sending OrderCreatedEvent to queue {}: orderId={}",
                JmsConfig.ORDER_CREATED_QUEUE, message.orderId());
        jmsTemplate.convertAndSend(JmsConfig.ORDER_CREATED_QUEUE, message);
    }

    public void sendOrderStatusChangedEvent(OrderStatusChangedMessage message) {
        logger.info("Sending OrderStatusChangedEvent to queue {}: orderId={}, oldStatus={}, newStatus={}",
                JmsConfig.ORDER_STATUS_CHANGED_QUEUE, message.orderId(), message.oldStatus(), message.newStatus());
        jmsTemplate.convertAndSend(JmsConfig.ORDER_STATUS_CHANGED_QUEUE, message);
    }

    public void sendOrderItemAddedEvent(OrderItemAddedMessage message) {
        logger.info("Sending OrderItemAddedEvent to queue {}: orderId={}, itemId={}",
                JmsConfig.ORDER_ITEM_ADDED_QUEUE, message.orderId(), message.itemId());
        jmsTemplate.convertAndSend(JmsConfig.ORDER_ITEM_ADDED_QUEUE, message);
    }

    public void sendOrderItemRemovedEvent(OrderItemRemovedMessage message) {
        logger.info("Sending OrderItemRemovedEvent to queue {}: orderId={}, itemId={}",
                JmsConfig.ORDER_ITEM_REMOVED_QUEUE, message.orderId(), message.itemId());
        jmsTemplate.convertAndSend(JmsConfig.ORDER_ITEM_REMOVED_QUEUE, message);
    }

    public void sendProductCreatedEvent(ProductCreatedMessage message) {
        logger.info("Sending ProductCreatedEvent to queue {}: productId={}",
                JmsConfig.PRODUCT_CREATED_QUEUE, message.productId());
        jmsTemplate.convertAndSend(JmsConfig.PRODUCT_CREATED_QUEUE, message);
    }

    public void sendProductStockUpdatedEvent(ProductStockUpdatedMessage message) {
        logger.info("Sending ProductStockUpdatedEvent to queue {}: productId={}, stockChange={}",
                JmsConfig.PRODUCT_STOCK_UPDATED_QUEUE, message.productId(), message.stockChange());
        jmsTemplate.convertAndSend(JmsConfig.PRODUCT_STOCK_UPDATED_QUEUE, message);
    }
}
