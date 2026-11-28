package com.orderservice.controller;

import com.orderservice.client.PaymentClient;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

@RestController
@RequestMapping("/orders")
public class PayController {

    private final PaymentClient client;
    private final CircuitBreakerRegistry circuitBreakers;

    public PayController(PaymentClient client, CircuitBreakerRegistry circuitBreakers) {
        this.client = client;
        this.circuitBreakers = circuitBreakers;
    }

    @PostMapping("/pay-notimeout")
    public Map<String, Object> payNoTimeout(@RequestParam(defaultValue = "0") long delayMs) {
        return timed(() -> client.payNoTimeout(delayMs), false);
    }

    @PostMapping("/pay-timeout")
    public Map<String, Object> payTimeout(@RequestParam(defaultValue = "0") long delayMs) {
        return timed(() -> client.payWithTimeout(delayMs), true);
    }

    @PostMapping("/pay-retry")
    public Map<String, Object> payRetry(@RequestParam(required = false) String orderId,
                                        @RequestParam(defaultValue = "0") long delayMs,
                                        @RequestParam(defaultValue = "0") int failRate,
                                        @RequestParam(defaultValue = "false") boolean bad) {
        return timed(() -> client.payWithRetry(orderId, delayMs, failRate, bad), false);
    }

    @PostMapping("/pay-cb")
    public Map<String, Object> payCb(@RequestParam(defaultValue = "o1") String orderId,
                                     @RequestParam(defaultValue = "0") long delayMs,
                                     @RequestParam(defaultValue = "0") int failRate) {
        Map<String, Object> response = timed(() -> client.payWithCircuitBreaker(orderId, delayMs, failRate), false);
        response.put("circuitBreaker", circuitBreakers.circuitBreaker("payment-cb").getState().name());
        return response;
    }

    private Map<String, Object> timed(Supplier<Map<String, Object>> call, boolean pendingOnError) {
        Map<String, Object> response = new LinkedHashMap<>();
        long start = System.nanoTime();
        try {
            response.put("result", call.get());
            response.put("status", "OK");
        } catch (Exception e) {
            response.put("status", pendingOnError ? "payment pending" : "ERROR");
            response.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        response.put("elapsedMs", (System.nanoTime() - start) / 1_000_000);
        return response;
    }
}
