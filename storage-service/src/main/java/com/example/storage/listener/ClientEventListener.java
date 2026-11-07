package com.example.storage.listener;

import com.example.storage.event.ClientEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ClientEventListener {

    private static final Logger log = LoggerFactory.getLogger(ClientEventListener.class);

    @KafkaListener(topics = "client.events", groupId = "storage-service-group")
    public void onClientEvent(ClientEvent event) {
        log.info("[STORAGE] Received event: id={}, type={}, name={}, email={}, at={}",
                event.eventId(), event.eventType(), event.name(), event.email(), event.occurredAt());
    }
}