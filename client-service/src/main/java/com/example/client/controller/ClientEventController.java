package com.example.client.controller;

import com.example.client.dto.ClientRegistrationRequest;
import com.example.client.event.ClientEvent;
import com.example.client.kafka.ClientEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/client")
public class ClientEventController {

    private final ClientEventPublisher publisher;

    public ClientEventController(ClientEventPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody ClientRegistrationRequest request) {
        if (request.name() == null || request.name().isBlank()
                || request.email() == null || request.email().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name and email must not be empty");
        }
        ClientEvent event = new ClientEvent(UUID.randomUUID().toString(), "CLIENT_REGISTERED",
                request.name().trim(), request.email().trim(), Instant.now().toString());
        publisher.publish(event);
        return ResponseEntity.accepted()
                .body(Map.of("status", "PUBLISHED", "eventId", event.eventId()));
    }
}