package com.example.client.kafka;

import com.example.client.event.ClientEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ClientEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ClientEventPublisher.class);

    private final KafkaTemplate<String, ClientEvent> kafkaTemplate;

    public ClientEventPublisher(KafkaTemplate<String, ClientEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(ClientEvent event) {
        kafkaTemplate.send(KafkaTopicConfig.TOPIC, event.eventId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[CLIENT] Failed to publish event {}", event.eventId(), ex);
                    } else {
                        log.info("[CLIENT] Published event {} to {}-{} offset {}", event.eventId(),
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}