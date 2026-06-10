package com.ecommerce.inventory.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import com.ecommerce.shared.event.inventory.InventoryFailedEvent;
import com.ecommerce.shared.event.inventory.InventoryReservedEvent;
import com.ecommerce.shared.event.inventory.InventoryRestoredEvent;
import com.ecommerce.shared.event.inventory.InventoryUpdatedEvent;
import com.ecommerce.shared.event.order.OrderCancelledEvent;
import com.ecommerce.shared.event.order.OrderCreatedEvent;
import com.ecommerce.shared.event.order.OrderUpdatedEvent;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {
    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:ecommerce-order-group}")
    private String groupId;

    private Map<String, Object> consumerProps() {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return props;
    }

    private <T> ConsumerFactory<String, T> consumerFactory(
            Class<T> eventClass) {

        JacksonJsonDeserializer<T> deserializer = new JacksonJsonDeserializer<>(eventClass);

        deserializer.addTrustedPackages(
                "com.ecommerce.shared.event");

        deserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(
                consumerProps(),
                new StringDeserializer(),
                deserializer);
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> listenerFactory(
            Class<T> eventClass,
            DefaultErrorHandler errorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory(eventClass));
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InventoryReservedEvent> inventoryReservedEventKafkaListenerFactory(
            DefaultErrorHandler errorHandler) {
        return listenerFactory(
                InventoryReservedEvent.class,
                errorHandler);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InventoryRestoredEvent> inventoryRestoredEventKafkaListenerFactory(
            DefaultErrorHandler errorHandler) {
        return listenerFactory(
                InventoryRestoredEvent.class,
                errorHandler);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InventoryFailedEvent> inventoryFailedEventKafkaListenerFactory(
            DefaultErrorHandler errorHandler) {
        return listenerFactory(
                InventoryFailedEvent.class,
                errorHandler);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InventoryUpdatedEvent> inventoryUpdatedEventKafkaListenerFactory(
            DefaultErrorHandler errorHandler) {
        return listenerFactory(
                InventoryUpdatedEvent.class,
                errorHandler);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> orderCreatedKafkaListenerFactory(
            DefaultErrorHandler errorHandler) {
        return listenerFactory(
                OrderCreatedEvent.class,
                errorHandler);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderUpdatedEvent> orderUpdatedKafkaListenerFactory(
            DefaultErrorHandler errorHandler) {
        return listenerFactory(
                OrderUpdatedEvent.class,
                errorHandler);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCancelledEvent> orderCancelledKafkaListenerFactory(
            DefaultErrorHandler errorHandler) {
        return listenerFactory(
                OrderCancelledEvent.class,
                errorHandler);
    }
}
