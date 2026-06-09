package com.platform.gateway.fallback;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
@RequiredArgsConstructor
public class GatewayFallbackController {

    private final UserServiceFallbackHandler userServiceFallbackHandler;
    private final NotificationServiceFallbackHandler notificationServiceFallbackHandler;

    @GetMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> userServiceFallback() {
        return userServiceFallbackHandler.toResponse();
    }

    @GetMapping(value = "/notification", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> notificationServiceFallback() {
        return notificationServiceFallbackHandler.toResponse();
    }
}
