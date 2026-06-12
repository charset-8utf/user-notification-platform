package com.notification.service;

import com.notification.dto.NotificationEmailRequest;
import com.notification.domain.UserNotificationOperation;
import com.notification.exception.EmailDeliveryException;
import com.notification.metrics.NotificationMetrics;
import com.notification.service.port.EmailDeliveryPort;
import com.notification.service.port.UserCacheView;
import com.notification.service.port.UserLookupPort;
import java.util.Optional;

/**
 * Template Method (GoF): фиксированный алгоритм доставки с hook-методами в {@link NotificationServiceImpl}.
 */
abstract class AbstractNotificationDeliveryTemplate {

    protected abstract Optional<NotificationIdempotencyService> idempotency();

    protected abstract ErrorMessageTruncator errorMessageTruncator();

    protected abstract UserLookupPort userLookup();

    protected abstract EmailDeliveryPort emailDelivery();

    protected abstract NotificationMetrics notificationMetrics();

    protected abstract String mailFrom();

    protected abstract String buildSubject(UserNotificationOperation operation);

    protected abstract String buildBody(NotificationEmailRequest request);

    protected abstract void persistSuccess(NotificationEmailRequest request);

    protected abstract void persistFailure(NotificationEmailRequest request, String errorMessage);

    public final void deliverEmail(NotificationEmailRequest request) {
        if (idempotency().map(service -> service.isAlreadyProcessed(request.eventId())).orElse(false)) {
            notificationMetrics().duplicateSkipped();
            onDuplicateSkipped(request);
            return;
        }
        enrichFromLookup(request);
        deliverMessage(request);
    }

    protected void onDuplicateSkipped(NotificationEmailRequest request) {
        // hook для логирования в конкретной реализации
    }

    protected void enrichFromLookup(NotificationEmailRequest request) {
        userLookup().findByEmail(request.email())
                .ifPresent(view -> onLookupEnrichment(request, view));
    }

    protected void onLookupEnrichment(NotificationEmailRequest request, UserCacheView view) {
        // hook
    }

    protected void deliverMessage(NotificationEmailRequest request) {
        String body = buildBody(request);
        String subject = buildSubject(request.operation());
        try {
            emailDelivery().send(mailFrom(), request.email(), subject, body);
            persistSuccess(request);
            idempotency().ifPresent(service -> service.markProcessed(request.eventId()));
            notificationMetrics().emailSent(request.operation());
            onDeliverySuccess(request);
        } catch (Exception e) {
            notificationMetrics().emailFailed(request.operation());
            persistFailure(request, errorMessageTruncator().truncate(e.getMessage()));
            onDeliveryFailure(request, e);
            if (e instanceof EmailDeliveryException deliveryException) {
                throw deliveryException;
            }
            throw new EmailDeliveryException("Не удалось отправить письмо", e);
        }
    }

    protected void onDeliverySuccess(NotificationEmailRequest request) {
        // hook
    }

    protected void onDeliveryFailure(NotificationEmailRequest request, Exception cause) {
        // hook
    }

}
