package com.notification.controller;

import com.notification.dto.NotificationLogSummaryResponse;
import com.notification.service.NotificationLogQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications/logs")
@RequiredArgsConstructor
@Profile("rest")
public class NotificationLogController {

    private final NotificationLogQueryService notificationLogQueryService;

    @GetMapping(value = "/latest", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NotificationLogSummaryResponse> latest(
            @RequestParam String email,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(notificationLogQueryService.latestByEmail(email, jwt));
    }
}
