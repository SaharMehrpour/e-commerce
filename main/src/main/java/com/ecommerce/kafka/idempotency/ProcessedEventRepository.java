package com.ecommerce.kafka.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository
        extends JpaRepository<ProcessedEvent, String> {
}
