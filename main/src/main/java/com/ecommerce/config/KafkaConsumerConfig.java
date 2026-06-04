package com.ecommerce.config;

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

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:ecommerce-group}")
    private String groupId;

    private Map<String, Object> consumerProps() {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return props;
    }

    private <T> ConsumerFactory<String, T> consumerFactory(Class<T> eventClass) {

        JacksonJsonDeserializer<T> deserializer =
                new JacksonJsonDeserializer<>(eventClass);

        deserializer.addTrustedPackages("com.ecommerce.event");
        deserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(
                consumerProps(),
                new StringDeserializer(),
                deserializer
        );
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T>
    kafkaListenerContainerFactory(Class<T> eventClass) {

        ConcurrentKafkaListenerContainerFactory<String, T> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory(eventClass));
        factory.setCommonErrorHandler(errorHandler());

        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent>
    orderCreatedKafkaListenerFactory() {
        return kafkaListenerContainerFactory(OrderCreatedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCancelledEvent>
    orderCancelledKafkaListenerFactory() {
        return kafkaListenerContainerFactory(OrderCancelledEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderUpdatedEvent>
    orderUpdatedKafkaListenerFactory() {
        return kafkaListenerContainerFactory(OrderUpdatedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InventoryReservedEvent>
    inventoryReservedEventKafkaListenerFactory() {
        return kafkaListenerContainerFactory(InventoryReservedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InventoryRestoredEvent>
    inventoryRestoredEventKafkaListenerFactory() {
        return kafkaListenerContainerFactory(InventoryRestoredEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InventoryFailedEvent>
    inventoryFailedEventKafkaListenerFactory() {
        return kafkaListenerContainerFactory(InventoryFailedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InventoryUpdatedEvent>
    inventoryUpdatedEventKafkaListenerFactory() {
        return kafkaListenerContainerFactory(InventoryUpdatedEvent.class);
    }

    @Bean
    public DefaultErrorHandler errorHandler() {
        FixedBackOff backOff = new FixedBackOff(1000L, 3);

        return new DefaultErrorHandler(
                (record, exception) -> System.err.println(
                        "Skipping message due to error: " + exception.getMessage()
                ),
                backOff
        );
    }
}