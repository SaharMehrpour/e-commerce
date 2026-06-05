package com.ecommerce.infrastructure.kafka.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import com.ecommerce.shared.event.InventoryFailedEvent;
import com.ecommerce.shared.event.InventoryReservedEvent;
import com.ecommerce.shared.event.InventoryRestoredEvent;
import com.ecommerce.shared.event.InventoryUpdatedEvent;
import com.ecommerce.shared.event.OrderCancelledEvent;
import com.ecommerce.shared.event.OrderCreatedEvent;
import com.ecommerce.shared.event.OrderUpdatedEvent;

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
    kafkaListenerContainerFactory(Class<T> eventClass, DefaultErrorHandler errorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, T> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory(eventClass));
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent>
    orderCreatedKafkaListenerFactory(DefaultErrorHandler errorHandler) {
        return kafkaListenerContainerFactory(OrderCreatedEvent.class, errorHandler);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCancelledEvent>
    orderCancelledKafkaListenerFactory(DefaultErrorHandler errorHandler) {
        return kafkaListenerContainerFactory(OrderCancelledEvent.class, errorHandler);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderUpdatedEvent>
    orderUpdatedKafkaListenerFactory(DefaultErrorHandler errorHandler) {
        return kafkaListenerContainerFactory(OrderUpdatedEvent.class, errorHandler);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InventoryReservedEvent>
    inventoryReservedEventKafkaListenerFactory(DefaultErrorHandler errorHandler) {
        return kafkaListenerContainerFactory(InventoryReservedEvent.class, errorHandler);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InventoryRestoredEvent>
    inventoryRestoredEventKafkaListenerFactory(DefaultErrorHandler errorHandler) {
        return kafkaListenerContainerFactory(InventoryRestoredEvent.class, errorHandler);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InventoryFailedEvent>
    inventoryFailedEventKafkaListenerFactory(DefaultErrorHandler errorHandler) {
        return kafkaListenerContainerFactory(InventoryFailedEvent.class, errorHandler);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InventoryUpdatedEvent>
    inventoryUpdatedEventKafkaListenerFactory(DefaultErrorHandler errorHandler) {
        return kafkaListenerContainerFactory(InventoryUpdatedEvent.class, errorHandler);
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, Object> kafkaTemplate) {

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, exception) -> new TopicPartition(record.topic() + "-dlt", record.partition())
                );

        FixedBackOff backOff = new FixedBackOff(1000L, 3);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        errorHandler.setRetryListeners((record, ex, deliveryAttempt) -> {
        System.out.println("🔁 Retry attempt " + deliveryAttempt +
                    " | topic=" + record.topic() +
                    " | key=" + record.key() +
                    " | error=" + ex.getMessage());
        });

        return errorHandler;
    }
}