package com.auditservice.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClientEvent(String eventId, String eventType, String name,
                          String email, String occurredAt) {
}