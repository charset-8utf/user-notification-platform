package com.notification.idempotency;

import com.notification.inbox.NotificationInboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Идемпотентность consumer: дубликаты определяются по inbox со статусом PROCESSED.
 */
@Service
@Profile("kafka")
@Slf4j
@RequiredArgsConstructor
public class NotificationIdempotencyService {

    private final NotificationInboxService inboxService;

    public boolean isAlreadyProcessed(UUID eventId) {
        return inboxService.isAlreadyProcessed(eventId);
    }

    public void markProcessed(UUID eventId) {
        inboxService.markProcessed(eventId);
    }
}
