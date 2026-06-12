package com.notification.service;

import com.notification.config.NotificationProperties;
import com.notification.domain.NotificationDeliveryStatus;
import com.notification.domain.UserNotificationOperation;
import com.notification.dto.NotificationEmailRequest;
import com.notification.mapper.NotificationLogMapper;
import com.notification.metrics.NotificationMetrics;
import com.notification.repository.NotificationLogRepository;
import com.notification.service.email.EmailContentStrategy;
import com.notification.service.email.EmailContentStrategyFactory;
import com.notification.service.port.EmailDeliveryPort;
import com.notification.service.port.UserCacheView;
import com.notification.service.port.UserLookupPort;
import com.platform.commons.audit.AuditLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Facade (GoF) над pipeline доставки email.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl extends AbstractNotificationDeliveryTemplate implements NotificationService {

    private final EmailDeliveryPort emailDelivery;
    private final NotificationLogRepository notificationLogRepository;
    private final NotificationLogMapper notificationLogMapper;
    private final UserLookupPort userLookup;
    private final Optional<NotificationIdempotencyService> idempotency;
    private final NotificationMetrics notificationMetrics;
    private final EmailContentStrategyFactory emailContentStrategyFactory;
    private final NotificationProperties notificationProperties;
    private final ErrorMessageTruncator errorMessageTruncator;

    @Override
    @AuditLog(action = "NOTIFICATION_EMAIL_SEND", resourceType = "notification")
    public void sendEmailNotification(NotificationEmailRequest request) {
        deliverEmail(request);
    }

    @Override
    protected Optional<NotificationIdempotencyService> idempotency() {
        return idempotency;
    }

    @Override
    protected UserLookupPort userLookup() {
        return userLookup;
    }

    @Override
    protected EmailDeliveryPort emailDelivery() {
        return emailDelivery;
    }

    @Override
    protected NotificationMetrics notificationMetrics() {
        return notificationMetrics;
    }

    @Override
    protected ErrorMessageTruncator errorMessageTruncator() {
        return errorMessageTruncator;
    }

    @Override
    protected String mailFrom() {
        return notificationProperties.mailFrom();
    }

    @Override
    protected String buildSubject(UserNotificationOperation operation) {
        return emailContentStrategyFactory.forOperation(operation).subject();
    }

    @Override
    protected String buildBody(NotificationEmailRequest request) {
        EmailContentStrategy strategy = emailContentStrategyFactory.forOperation(request.operation());
        return strategy.body(notificationProperties.siteName());
    }

    @Override
    protected void persistSuccess(NotificationEmailRequest request) {
        notificationLogRepository.save(
                notificationLogMapper.toEntity(request, NotificationDeliveryStatus.SENT, null));
    }

    @Override
    protected void persistFailure(NotificationEmailRequest request, String errorMessage) {
        notificationLogRepository.save(
                notificationLogMapper.toEntity(request, NotificationDeliveryStatus.FAILED, errorMessage));
    }

    @Override
    protected void onDuplicateSkipped(NotificationEmailRequest request) {
        log.info("Пропуск дубликата уведомления: eventId={}", request.eventId());
    }

    @Override
    protected void onLookupEnrichment(NotificationEmailRequest request, UserCacheView view) {
        log.debug(
                "Обогащение из Redis: user.id={}, cache.email={}, status={}",
                view.id(), view.email(), view.status());
    }

    @Override
    protected void onDeliverySuccess(NotificationEmailRequest request) {
        log.info("Отправлено уведомление: operation={}, email={}", request.operation(), request.email());
    }

    @Override
    protected void onDeliveryFailure(NotificationEmailRequest request, Exception cause) {
        log.error("Не удалось отправить уведомление: operation={}, email={}",
                request.operation(), request.email(), cause);
    }
}
