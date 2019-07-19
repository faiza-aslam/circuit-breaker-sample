package com.examples.circuitbreaker.annotation;

import com.examples.circuitbreaker.type.CircuitBreakerHost;

import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface CustomBreaker {
    CircuitBreakerHost value();
}
