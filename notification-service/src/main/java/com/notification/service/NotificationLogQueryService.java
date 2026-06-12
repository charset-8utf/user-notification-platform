package com.notification.service;

import com.notification.dto.NotificationLogSummaryResponse;
import com.notification.mapper.NotificationLogMapper;
import com.notification.repository.NotificationLogRepository;
import com.notification.security.NotificationLogAccessPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationLogQueryService {

    private final NotificationLogRepository notificationLogRepository;
    private final NotificationLogMapper notificationLogMapper;
    private final NotificationLogAccessPolicy notificationLogAccessPolicy;

    @Transactional(readOnly = true)
    public NotificationLogSummaryResponse latestByEmail(String email, Jwt jwt) {
        notificationLogAccessPolicy.assertCanRead(email, jwt);
        return notificationLogRepository.findFirstByEmailOrderByCreatedAtDesc(email)
                .map(notificationLogMapper::toSummary)
                .orElseGet(() -> new NotificationLogSummaryResponse(
                        false,
                        null,
                        null,
                        null,
                        email,
                        null,
                        "Уведомлений для этого email пока нет"));
    }
}
