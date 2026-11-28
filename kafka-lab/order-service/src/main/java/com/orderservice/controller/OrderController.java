package com.orderservice.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final String[] TYPES = {"CREATED", "PAID", "SHIPPED"};

    private final KafkaTemplate<String, String> keyedTemplate;
    private final KafkaTemplate<String, String> rrTemplate;

    public OrderController(@Qualifier("keyedTemplate") KafkaTemplate<String, String> keyedTemplate,
                           @Qualifier("rrTemplate") KafkaTemplate<String, String> rrTemplate) {
        this.keyedTemplate = keyedTemplate;
        this.rrTemplate = rrTemplate;
    }

    @PostMapping("/send")
    public Map<String, Object> send(@RequestParam(defaultValue = "true") boolean keyed,
                                    @RequestParam(defaultValue = "10") int customers) {
        publish("orders", keyed, customers);
        return Map.of("topic", "orders", "keyed", keyed, "events", customers * 3);
    }

    @PostMapping("/send-db")
    public Map<String, Object> sendDb(@RequestParam(defaultValue = "1") int customers) {
        publish("orders-db", true, customers);
        return Map.of("topic", "orders-db", "keyed", true, "events", customers * 3);
    }

    private void publish(String topic, boolean useKey, int customers) {
        for (int c = 1; c <= customers; c++) {
            String customerId = "C" + c;
            for (int seq = 1; seq <= 3; seq++) {
                String value = customerId + "|" + TYPES[seq - 1] + "|" + seq + "|" + UUID.randomUUID();
                if (useKey) {
                    keyedTemplate.send(topic, customerId, value);
                } else {
                    rrTemplate.send(topic, value);
                }
            }
        }
        keyedTemplate.flush();
        rrTemplate.flush();
    }
}
