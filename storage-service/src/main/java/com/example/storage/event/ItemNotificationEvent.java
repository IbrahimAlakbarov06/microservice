package com.example.storage.event;

public record ItemNotificationEvent(Long itemId, String name, String description,
                                    String recipientEmail, String createdAt) {
}