package com.notification.controller;

import com.notification.dto.NotificationEmailRequest;
import com.notification.service.NotificationService;
import com.platform.commons.audit.AuditLog;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Profile("rest")
public class NotificationController {

    private final NotificationService notificationService;

    @AuditLog(action = "NOTIFICATION_EMAIL_SEND", resourceType = "notification")
    @PostMapping(value = "/email", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendEmailNotification(@Valid @RequestBody NotificationEmailRequest request) {
        notificationService.sendEmailNotification(request);
        return ResponseEntity.noContent().build();
    }
}
