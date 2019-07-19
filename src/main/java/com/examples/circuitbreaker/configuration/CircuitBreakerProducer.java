package com.examples.circuitbreaker.configuration;

import com.examples.circuitbreaker.annotation.CustomBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import java.time.Duration;
import java.util.Date;

import static com.examples.circuitbreaker.type.CircuitBreakerHost.SERVICE_A;

public class CircuitBreakerProducer {

    private CircuitBreakerRegistry circuitBreakerRegistry;

    @PostConstruct
    public void init () {
        /* Default Config */
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(5))
                .ringBufferSizeInClosedState(6)
                .ringBufferSizeInHalfOpenState(2)
                .build();

        circuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig);
        circuitBreakerRegistry.getEventPublisher()
                .onEntryAdded(e -> System.out.println(new Date() + " - CustomBreaker added: "+ e.getAddedEntry().getName()))
                .onEntryRemoved(e -> System.out.println("CustomBreaker removed: "+ e.getRemovedEntry().getName()));
    }

    @Produces
    @CustomBreaker(SERVICE_A)
    public CircuitBreaker serviceACircuitBreaker() {
        System.out.println(" ##### serviceACircuitBreaker ##### ");
        return circuitBreakerRegistry.circuitBreaker(SERVICE_A.toString());
    }
}
