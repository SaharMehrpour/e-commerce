package com.ecommerce.event;

import java.time.Instant;

public abstract class Event {

    private String eventId;
    private String eventType;
    private Instant createdAt;

    public Event() {
    }

    public Event(
            String eventId,
            String eventType,
            Instant createdAt
    ) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.createdAt = createdAt;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}