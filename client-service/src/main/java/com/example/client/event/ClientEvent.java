package com.example.client.event;

public record ClientEvent(String eventId, String eventType, String name,
                          String email, String occurredAt) {
}