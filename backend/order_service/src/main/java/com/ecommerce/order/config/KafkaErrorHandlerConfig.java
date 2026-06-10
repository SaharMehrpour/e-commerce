package com.ecommerce.order.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErrorHandlerConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<String, Object> kafkaTemplate) {

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> new TopicPartition(
                        record.topic() + "-dlt",
                        record.partition()));

        FixedBackOff backOff = new FixedBackOff(
                1000L,
                3);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                recoverer,
                backOff);

        errorHandler.setRetryListeners(
                (record, ex, deliveryAttempt) -> System.out.println(
                        "🔁 Retry attempt " + deliveryAttempt +
                        " | key=" + record.key() +
                        " | error=" + ex.getMessage()));

        return errorHandler;
    }
}