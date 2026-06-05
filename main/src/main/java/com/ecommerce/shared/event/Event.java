package com.ecommerce.shared.event;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class Event {

    private String eventId;
    private String eventType;
    private String createdAt;

    public Event(
            String eventId,
            String eventType,
            String createdAt
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}