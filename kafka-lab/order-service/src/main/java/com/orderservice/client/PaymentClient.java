package com.orderservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;

@Component
public class PaymentClient {

    private static final Logger log = LoggerFactory.getLogger(PaymentClient.class);

    private final RestTemplate plain;
    private final RestTemplate withTimeouts;

    @Value("${payment.url}")
    private String url;

    public PaymentClient(@Qualifier("plainRestTemplate") RestTemplate plain,
                         @Qualifier("timeoutRestTemplate") RestTemplate withTimeouts) {
        this.plain = plain;
        this.withTimeouts = withTimeouts;
    }

    public Map<String, Object> payNoTimeout(long delayMs) {
        return call(plain, delayMs, 0, null, false);
    }

    public Map<String, Object> payWithTimeout(long delayMs) {
        return call(withTimeouts, delayMs, 0, null, false);
    }

    @Retry(name = "payment-retry")
    public Map<String, Object> payWithRetry(String orderId, long delayMs, int failRate, boolean bad) {
        return call(withTimeouts, delayMs, failRate, orderId, bad);
    }

    @CircuitBreaker(name = "payment-cb", fallbackMethod = "cbFallback")
    public Map<String, Object> payWithCircuitBreaker(String orderId, long delayMs, int failRate) {
        return call(withTimeouts, delayMs, failRate, orderId, false);
    }

    public Map<String, Object> cbFallback(String orderId, long delayMs, int failRate, Throwable t) {
        return Map.of("status", "payment pending", "fallback", true, "reason", t.getClass().getSimpleName());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Map<String, Object> call(RestTemplate rt, long delayMs, int failRate, String idempotencyKey, boolean bad) {
        log.info("ATTEMPT at {} -> POST /pay delayMs={} failRate={} bad={} key={}",
                Instant.now(), delayMs, failRate, bad, idempotencyKey);
        HttpHeaders headers = new HttpHeaders();
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            headers.set("Idempotency-Key", idempotencyKey);
        }
        String target = url + "?delayMs=" + delayMs + "&failRate=" + failRate + "&bad=" + bad;
        ResponseEntity<Map> response = rt.exchange(target, HttpMethod.POST, new HttpEntity<>(headers), Map.class);
        return (Map<String, Object>) response.getBody();
    }
}
