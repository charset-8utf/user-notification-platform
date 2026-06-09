package com.notification.service;

import com.notification.dto.NotificationEmailRequest;
import com.notification.entity.UserNotificationOperation;
import com.notification.exception.EmailDeliveryException;
import com.notification.idempotency.NotificationIdempotencyService;
import com.notification.lookup.UserLookupPort;
import com.notification.metrics.NotificationMetrics;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

abstract class AbstractNotificationDeliveryTemplate {

    protected static final int MAX_ERROR_MESSAGE_LENGTH = 2000;

    protected abstract Optional<NotificationIdempotencyService> idempotency();

    protected abstract UserLookupPort userLookup();

    protected abstract NotificationMetrics notificationMetrics();

    protected abstract String mailFrom();

    protected abstract org.springframework.mail.javamail.JavaMailSender mailSender();

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

    protected void onLookupEnrichment(NotificationEmailRequest request, com.notification.lookup.UserCacheView view) {
        // hook
    }

    protected void deliverMessage(NotificationEmailRequest request) {
        String body = buildBody(request);
        String subject = buildSubject(request.operation());
        try {
            MimeMessage message = mailSender().createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setFrom(mailFrom());
            helper.setTo(request.email());
            helper.setSubject(subject);
            helper.setText(body, false);
            mailSender().send(message);
            persistSuccess(request);
            idempotency().ifPresent(service -> service.markProcessed(request.eventId()));
            notificationMetrics().emailSent(request.operation());
            onDeliverySuccess(request);
        } catch (Exception e) {
            notificationMetrics().emailFailed(request.operation());
            persistFailure(request, truncate(e.getMessage()));
            onDeliveryFailure(request, e);
            throw new EmailDeliveryException("Не удалось отправить письмо", e);
        }
    }

    protected void onDeliverySuccess(NotificationEmailRequest request) {
        // hook
    }

    protected void onDeliveryFailure(NotificationEmailRequest request, Exception cause) {
        // hook
    }

    protected static String truncate(String message) {
        if (message == null) {
            return null;
        }
        return message.length() <= MAX_ERROR_MESSAGE_LENGTH
                ? message
                : message.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }
}
