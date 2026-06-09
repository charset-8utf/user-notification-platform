package com.notification.service;

import com.notification.dto.NotificationEmailRequest;
import com.notification.email.EmailContentStrategy;
import com.notification.email.EmailContentStrategyFactory;
import com.notification.entity.NotificationDeliveryStatus;
import com.notification.entity.UserNotificationOperation;
import com.notification.idempotency.NotificationIdempotencyService;
import com.notification.lookup.UserCacheView;
import com.notification.lookup.UserLookupPort;
import com.notification.mapper.NotificationLogMapper;
import com.notification.metrics.NotificationMetrics;
import com.platform.commons.audit.AuditLog;
import com.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Реализация сервиса уведомлений.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl extends AbstractNotificationDeliveryTemplate implements NotificationService {

    private final JavaMailSender mailSender;
    private final NotificationLogRepository notificationLogRepository;
    private final NotificationLogMapper notificationLogMapper;
    private final UserLookupPort userLookup;
    private final Optional<NotificationIdempotencyService> idempotency;
    private final NotificationMetrics notificationMetrics;
    private final EmailContentStrategyFactory emailContentStrategyFactory;

    @Value("${app.notification.site-name}")
    private String siteName;

    @Value("${app.notification.mail-from}")
    private String mailFrom;

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
    protected NotificationMetrics notificationMetrics() {
        return notificationMetrics;
    }

    @Override
    protected String mailFrom() {
        return mailFrom;
    }

    @Override
    protected JavaMailSender mailSender() {
        return mailSender;
    }

    @Override
    protected String buildSubject(UserNotificationOperation operation) {
        return emailContentStrategyFactory.forOperation(operation).subject();
    }

    @Override
    protected String buildBody(NotificationEmailRequest request) {
        EmailContentStrategy strategy = emailContentStrategyFactory.forOperation(request.operation());
        return strategy.body(siteName);
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
                "Redis enrichment: user.id={}, cache.email={}, status={}",
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
