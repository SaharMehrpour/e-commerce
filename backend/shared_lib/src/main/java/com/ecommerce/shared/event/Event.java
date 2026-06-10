package com.ecommerce.shared.event;

import java.time.Instant;
import java.util.UUID;

public class Event {

    private String eventId;
    private EventType eventType;
    private String createdAt;

    public Event() {}

    public Event(EventType eventType) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.createdAt = Instant.now().toString();
    }

    public String getEventId() {
        return eventId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}