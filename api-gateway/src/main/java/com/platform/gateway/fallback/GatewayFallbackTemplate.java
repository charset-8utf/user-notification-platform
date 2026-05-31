package com.platform.gateway.fallback;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class GatewayFallbackTemplate {

    protected abstract String serviceName();

    public ResponseEntity<Map<String, Object>> toResponse() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "Service Unavailable");
        body.put("service", serviceName());
        body.put("message", fallbackMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    protected String fallbackMessage() {
        return "Circuit breaker open or downstream timeout";
    }
}
