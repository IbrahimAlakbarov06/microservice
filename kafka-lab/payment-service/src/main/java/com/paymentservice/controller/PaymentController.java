package com.paymentservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@RestController
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final Map<String, Map<String, Object>> store = new ConcurrentHashMap<>();

    @PostMapping("/pay")
    public ResponseEntity<Map<String, Object>> pay(
            @RequestParam(defaultValue = "0") long delayMs,
            @RequestParam(defaultValue = "0") int failRate,
            @RequestParam(defaultValue = "false") boolean bad,
            @RequestHeader(value = "Idempotency-Key", required = false) String key) throws InterruptedException {

        log.info("POST /pay delayMs={} failRate={} bad={} key={}", delayMs, failRate, bad, key);

        if (bad) {
            return ResponseEntity.badRequest().body(Map.of("error", "bad request"));
        }

        Map<String, Object> result = Map.of(
                "paymentId", UUID.randomUUID().toString(),
                "status", "PAID");

        if (key != null) {

            Map<String, Object> previous = store.putIfAbsent(key, result);
            if (previous != null) {
                log.info("REPEAT key={} -> returning stored result {}", key, previous);
                return ResponseEntity.ok(previous);
            }
        }

        Thread.sleep(delayMs);

        if (ThreadLocalRandom.current().nextInt(100) < failRate) {
            if (key != null) {
                store.remove(key);
            }
            log.info("FAILED with 503 key={}", key);
            return ResponseEntity.status(503).body(Map.of("error", "unavailable"));
        }

        log.info("PAYMENT CREATED {}", result);
        return ResponseEntity.ok(result);
    }
}
