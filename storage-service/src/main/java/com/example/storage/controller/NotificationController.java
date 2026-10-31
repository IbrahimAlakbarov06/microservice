package com.example.storage.controller;

import com.example.storage.config.RabbitConfig;
import com.example.storage.event.ItemNotificationEvent;
import com.example.storage.model.Item;
import com.example.storage.model.ItemRepository;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/storage")
public class NotificationController {

    public record NotifyRequest(String email) {
    }

    private final ItemRepository repository;
    private final RabbitTemplate rabbitTemplate;

    public NotificationController(ItemRepository repository, RabbitTemplate rabbitTemplate) {
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping("/items/{id}/notify")
    public ResponseEntity<Map<String, Object>> notifyItem(@PathVariable Long id,
                                                          @RequestBody NotifyRequest request) {
        if (request.email() == null || request.email().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email must not be empty");
        }
        Item item = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found: " + id));

        ItemNotificationEvent event = new ItemNotificationEvent(
                item.getId(), item.getName(), item.getDescription(),
                request.email(), Instant.now().toString());
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, event);
        } catch (AmqpException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Message broker unavailable");
        }
        return ResponseEntity.accepted()
                .body(Map.of("status", "QUEUED", "itemId", id, "recipient", request.email()));
    }
}