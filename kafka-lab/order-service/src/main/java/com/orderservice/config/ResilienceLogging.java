package com.orderservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceLogging {

    private static final Logger log = LoggerFactory.getLogger(ResilienceLogging.class);

    public ResilienceLogging(CircuitBreakerRegistry registry) {
        registry.circuitBreaker("payment-cb").getEventPublisher()
                .onStateTransition(e -> log.info("CIRCUIT BREAKER {} -> {}",
                        e.getStateTransition().getFromState(), e.getStateTransition().getToState()));
    }
}
