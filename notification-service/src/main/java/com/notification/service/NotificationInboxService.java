package com.notification.service;

import com.notification.domain.InboxStatus;
import com.notification.dto.NotificationEmailRequest;
import com.notification.entity.NotificationInbox;
import com.notification.repository.NotificationInboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Бизнес-логика transactional inbox: приём, идемпотентность, смена статусов.
 */
@Service
@Profile("kafka")
@Slf4j
@RequiredArgsConstructor
public class NotificationInboxService {

    private final NotificationInboxRepository inboxRepository;

    public boolean isAlreadyProcessed(UUID eventId) {
        return inboxRepository.findById(eventId.toString())
                .map(row -> row.getStatus() == InboxStatus.PROCESSED)
                .orElse(false);
    }

    public List<NotificationInbox> claimPendingBatch(int batchSize) {
        return inboxRepository.claimPendingBatch(batchSize);
    }

    public void requeueStaleProcessing(long timeoutMs) {
        LocalDateTime staleBefore = LocalDateTime.now().minus(Duration.ofMillis(timeoutMs));
        long requeued = inboxRepository.requeueStaleProcessing(staleBefore);
        if (requeued > 0) {
            log.info("Возвращено в PENDING {} зависших PROCESSING-записей inbox", requeued);
        }
    }

    public List<NotificationInbox> findFailedBatch(int batchSize) {
        return inboxRepository.findByStatusOrderByReceivedAtAsc(
                InboxStatus.FAILED, PageRequest.of(0, batchSize));
    }

    /**
     * Сохраняет событие в inbox. Дубликат с тем же eventId не перезаписывает PROCESSED/FAILED.
     */
    public void enqueue(NotificationEmailRequest request) {
        if (inboxRepository.existsById(request.eventId().toString())) {
            log.debug("Inbox уже содержит eventId={}", request.eventId());
            return;
        }
        try {
            inboxRepository.save(NotificationInbox.pending(
                    request.eventId(), request.operation(), request.email()));
            log.debug("Событие записано в inbox: eventId={}, operation={}, email={}",
                    request.eventId(), request.operation(), request.email());
        } catch (DuplicateKeyException ex) {
            log.debug("Параллельная запись inbox eventId={}", request.eventId());
        }
    }

    public void markProcessed(UUID eventId) {
        int updated = (int) inboxRepository.updateStatus(
                eventId.toString(), InboxStatus.PROCESSED, LocalDateTime.now(), InboxStatus.PROCESSING);
        if (updated == 0) {
            log.debug("Inbox eventId={} не помечен PROCESSED (уже обработан другим relay)", eventId);
        }
    }

    public void markFailed(UUID eventId) {
        int updated = (int) inboxRepository.updateStatus(
                eventId.toString(), InboxStatus.FAILED, LocalDateTime.now(), InboxStatus.PROCESSING);
        if (updated == 0) {
            log.debug("Inbox eventId={} не помечен FAILED (уже обработан другим relay)", eventId);
        }
    }

    public int requeueFailed(UUID eventId) {
        return (int) inboxRepository.updateStatus(
                eventId.toString(), InboxStatus.PENDING, null, InboxStatus.FAILED);
    }
}
