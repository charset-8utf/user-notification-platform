package com.notification.idempotency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Регистрирует {@code eventId} после успешной обработки; дубликаты при redelivery пропускаются.
 */
@Service
@Profile("kafka")
@Slf4j
@RequiredArgsConstructor
public class NotificationIdempotencyService {

    private final ProcessedNotificationEventRepository repository;

    public boolean isAlreadyProcessed(UUID eventId) {
        return repository.existsById(eventId.toString());
    }

    public void markProcessed(UUID eventId) {
        if (repository.existsById(eventId.toString())) {
            return;
        }
        try {
            repository.save(new ProcessedNotificationEvent(eventId));
        } catch (DuplicateKeyException ex) {
            log.debug("Параллельная регистрация eventId={}", eventId);
        }
    }
}
