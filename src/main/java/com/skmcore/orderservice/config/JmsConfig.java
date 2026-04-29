package com.skmcore.orderservice.config;

import org.apache.activemq.artemis.api.core.QueueConfiguration;
import org.apache.activemq.artemis.jms.server.config.ConnectionFactoryConfiguration;
import org.apache.activemq.artemis.jms.server.config.JMSQueueConfiguration;
import org.apache.activemq.artemis.jms.server.config.impl.ConnectionFactoryConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.JMSQueueConfigurationImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.JacksonJsonMessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import tools.jackson.databind.json.JsonMapper;

import jakarta.jms.ConnectionFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Configuration
@EnableJms
public class JmsConfig {

    // Queue names
    public static final String ORDER_CREATED_QUEUE = "order.created";
    public static final String ORDER_STATUS_CHANGED_QUEUE = "order.status.changed";
    public static final String ORDER_ITEM_ADDED_QUEUE = "order.item.added";
    public static final String ORDER_ITEM_REMOVED_QUEUE = "order.item.removed";
    public static final String PRODUCT_CREATED_QUEUE = "product.created";
    public static final String PRODUCT_STOCK_UPDATED_QUEUE = "product.stock.updated";

    /**
     * Message converter for JSON serialization/deserialization.
     */
    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        // Configure JsonMapper (Jackson 3)
        // Jackson 3 defaults to ISO-8601 for dates, no need to disable WRITE_DATES_AS_TIMESTAMPS
        JsonMapper mapper = JsonMapper.builder().build();

        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter(mapper);
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        
        // Map types to type IDs for interoperability if needed
        Map<String, Class<?>> typeIdMappings = new HashMap<>();
        converter.setTypeIdMappings(typeIdMappings);
        
        return converter;
    }

    /**
     * JmsListenerContainerFactory for consuming messages.
     */
    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(@Qualifier("jmsConnectionFactory") ConnectionFactory jmsConnectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(jmsConnectionFactory);
        factory.setMessageConverter(jacksonJmsMessageConverter());
        factory.setConcurrency("1-1");
        factory.setErrorHandler(throwable -> {
            // Log error and potentially implement retry logic
            org.slf4j.LoggerFactory.getLogger(JmsConfig.class)
                    .error("JMS error processing message", throwable);
        });
        return factory;
    }

    /**
     * JmsTemplate for producing messages.
     */
    @Bean
    public JmsTemplate jmsTemplate(@Qualifier("jmsConnectionFactory") ConnectionFactory jmsConnectionFactory) {
        JmsTemplate template = new JmsTemplate(jmsConnectionFactory);
        template.setMessageConverter(jacksonJmsMessageConverter());
        return template;
    }

    /**
     * Queue configurations for embedded Artemis.
     */
    @Bean
    public List<JMSQueueConfiguration> jmsQueueConfigurations() {
        return Arrays.asList(
            new JMSQueueConfigurationImpl().setName(ORDER_CREATED_QUEUE),
            new JMSQueueConfigurationImpl().setName(ORDER_STATUS_CHANGED_QUEUE),
            new JMSQueueConfigurationImpl().setName(ORDER_ITEM_ADDED_QUEUE),
            new JMSQueueConfigurationImpl().setName(ORDER_ITEM_REMOVED_QUEUE),
            new JMSQueueConfigurationImpl().setName(PRODUCT_CREATED_QUEUE),
            new JMSQueueConfigurationImpl().setName(PRODUCT_STOCK_UPDATED_QUEUE)
        );
    }

    /**
     * Connection factory configuration for embedded Artemis.
     */
    @Bean
    public ConnectionFactoryConfiguration connectionFactoryConfiguration() {
        return new ConnectionFactoryConfigurationImpl()
                .setName("DefaultConnectionFactory")
                .setBindings("ConnectionFactory");
    }
}
