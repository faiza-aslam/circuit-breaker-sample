package com.examples.circuitbreaker.client;

import com.examples.circuitbreaker.annotation.CustomBreaker;
import com.examples.circuitbreaker.type.CircuitBreakerHost;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.vavr.control.Try;

import javax.inject.Inject;
import java.util.Date;
import java.util.function.Supplier;

public class ServiceA {

    @Inject
    @CustomBreaker(value = CircuitBreakerHost.SERVICE_A)
    private CircuitBreaker circuitBreaker;

    public void setCircuitBreaker(CircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    public String getSuccess() {
        return executeWithBreaker(() -> "SUCCESS");
    }

    public String getFailure() {
        return executeWithBreaker(() -> {
            throw new RuntimeException("An unknown error occurred !!");
        });
    }

    private <T> T executeWithBreaker(Supplier<T> supplier) {
        Supplier<T> decoratedSupplier = io.github.resilience4j.circuitbreaker.CircuitBreaker.decorateSupplier(circuitBreaker, supplier);
        return Try
                .ofSupplier(decoratedSupplier)
                .recover(e -> {
                    System.out.println(new Date() + " ---> Error recovered: ->"+e);
                    return (T) "Recovered";
                })
                .get();
    }


}
