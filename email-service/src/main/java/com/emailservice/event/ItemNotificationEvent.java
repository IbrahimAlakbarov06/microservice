package com.emailservice.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ItemNotificationEvent(Long itemId, String name, String description,
                                    String recipientEmail, String createdAt) {
}