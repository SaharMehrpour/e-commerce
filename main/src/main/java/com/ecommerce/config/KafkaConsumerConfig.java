package com.ecommerce.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import com.ecommerce.event.InventoryFailedEvent;
import com.ecommerce.event.InventoryReservedEvent;
import com.ecommerce.event.InventoryRestoredEvent;
import com.ecommerce.event.InventoryUpdatedEvent;
import com.ecommerce.event.OrderCancelledEvent;
import com.ecommerce.event.OrderCreatedEvent;
import com.ecommerce.event.OrderUpdatedEvent;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, OrderCreatedEvent> createdConsumerFactory() {

        JacksonJsonDeserializer<OrderCreatedEvent> deserializer =
                        new JacksonJsonDeserializer<>(OrderCreatedEvent.class);

        deserializer.addTrustedPackages("com.ecommerce.event");
        deserializer.setUseTypeHeaders(false);

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "ecommerce-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent>
    orderCreatedKafkaListenerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(createdConsumerFactory());
        factory.setCommonErrorHandler(errorHandler());

        return factory;
    }

    @Bean
    public ConsumerFactory<String, OrderCancelledEvent> cancelledConsumerFactory() {

        JacksonJsonDeserializer<OrderCancelledEvent> deserializer =
                new JacksonJsonDeserializer<>(OrderCancelledEvent.class);

        deserializer.addTrustedPackages("com.ecommerce.event");
        deserializer.setUseTypeHeaders(false);

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "ecommerce-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCancelledEvent>
    orderCancelledKafkaListenerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, OrderCancelledEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(cancelledConsumerFactory());
        factory.setCommonErrorHandler(errorHandler());

        return factory;
    }

    @Bean
    public ConsumerFactory<String, OrderUpdatedEvent> updatedConsumerFactory() {

        JacksonJsonDeserializer<OrderUpdatedEvent> deserializer =
                new JacksonJsonDeserializer<>(OrderUpdatedEvent.class);

        deserializer.addTrustedPackages("com.ecommerce.event");
        deserializer.setUseTypeHeaders(false);

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "ecommerce-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderUpdatedEvent>
    orderUpdatedKafkaListenerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, OrderUpdatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(updatedConsumerFactory());
        factory.setCommonErrorHandler(errorHandler());

        return factory;
    }

    @Bean
    public ConsumerFactory<String, InventoryReservedEvent> inventoryReservedConsumerFactory() {

        JacksonJsonDeserializer<InventoryReservedEvent> deserializer = new JacksonJsonDeserializer<>(
                InventoryReservedEvent.class);

        deserializer.addTrustedPackages("com.ecommerce.event");
        deserializer.setUseTypeHeaders(false);

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "ecommerce-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InventoryReservedEvent> inventoryReservedEventKafkaListenerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, InventoryReservedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(inventoryReservedConsumerFactory());
        factory.setCommonErrorHandler(errorHandler());

        return factory;
    }

    @Bean
    public ConsumerFactory<String, InventoryRestoredEvent> inventoryRestoredConsumerFactory() {

        JacksonJsonDeserializer<InventoryRestoredEvent> deserializer = new JacksonJsonDeserializer<>(
                InventoryRestoredEvent.class);

        deserializer.addTrustedPackages("com.ecommerce.event");
        deserializer.setUseTypeHeaders(false);

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "ecommerce-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InventoryRestoredEvent> inventoryRestoredEventKafkaListenerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, InventoryRestoredEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(inventoryRestoredConsumerFactory());
        factory.setCommonErrorHandler(errorHandler());

        return factory;
    }

    @Bean
    public ConsumerFactory<String, InventoryFailedEvent> inventoryFailedConsumerFactory() {

        JacksonJsonDeserializer<InventoryFailedEvent> deserializer = new JacksonJsonDeserializer<>(
                InventoryFailedEvent.class);

        deserializer.addTrustedPackages("com.ecommerce.event");
        deserializer.setUseTypeHeaders(false);

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "ecommerce-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InventoryFailedEvent> inventoryFailedEventKafkaListenerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, InventoryFailedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(inventoryFailedConsumerFactory());
        factory.setCommonErrorHandler(errorHandler());

        return factory;
    }

    @Bean
    public ConsumerFactory<String, InventoryUpdatedEvent> inventoryUpdatedConsumerFactory() {

        JacksonJsonDeserializer<InventoryUpdatedEvent> deserializer = new JacksonJsonDeserializer<>(
                InventoryUpdatedEvent.class);

        deserializer.addTrustedPackages("com.ecommerce.event");
        deserializer.setUseTypeHeaders(false);

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "ecommerce-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InventoryUpdatedEvent> inventoryUpdatedEventKafkaListenerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, InventoryUpdatedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(inventoryUpdatedConsumerFactory());
        factory.setCommonErrorHandler(errorHandler());

        return factory;
    }

    @Bean
    public DefaultErrorHandler errorHandler() {
        FixedBackOff backOff = new FixedBackOff(1000L, 3);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler((record, exception) -> {
            System.err.println("Skipping message due to error: " + exception.getMessage());
        }, backOff);
        
        return errorHandler;
    }
}