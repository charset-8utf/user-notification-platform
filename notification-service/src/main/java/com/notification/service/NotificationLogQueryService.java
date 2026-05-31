package com.notification.service;

import com.notification.dto.NotificationLogSummaryResponse;
import com.notification.entity.NotificationLog;
import com.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationLogQueryService {

    private final NotificationLogRepository notificationLogRepository;

    @Transactional(readOnly = true)
    public NotificationLogSummaryResponse latestByEmail(String email) {
        return notificationLogRepository.findFirstByEmailOrderByCreatedAtDesc(email)
                .map(this::toSummary)
                .orElseGet(() -> new NotificationLogSummaryResponse(
                        false,
                        null,
                        null,
                        null,
                        email,
                        null,
                        "Уведомлений для этого email пока нет"));
    }

    private NotificationLogSummaryResponse toSummary(NotificationLog log) {
        return new NotificationLogSummaryResponse(
                true,
                log.getOperation() != null ? log.getOperation().name() : null,
                log.getChannel() != null ? log.getChannel().name() : null,
                log.getStatus() != null ? log.getStatus().name() : null,
                log.getEmail(),
                log.getCreatedAt(),
                log.getErrorMessage() != null ? log.getErrorMessage() : "OK");
    }
}
