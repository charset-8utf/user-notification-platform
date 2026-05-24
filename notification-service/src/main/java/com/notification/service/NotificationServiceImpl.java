package com.notification.service;

import com.notification.dto.NotificationEmailRequest;
import com.notification.entity.NotificationDeliveryStatus;
import com.notification.entity.UserNotificationOperation;
import com.notification.exception.EmailDeliveryException;
import com.notification.idempotency.NotificationIdempotencyService;
import com.notification.lookup.UserLookupPort;
import com.notification.mapper.NotificationLogMapper;
import com.notification.metrics.NotificationMetrics;
import com.notification.repository.NotificationLogRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Реализация сервиса уведомлений.
 */
@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 2000;

    private final JavaMailSender mailSender;
    private final NotificationLogRepository notificationLogRepository;
    private final NotificationLogMapper notificationLogMapper;
    private final UserLookupPort userLookup;
    private final Optional<NotificationIdempotencyService> idempotency;
    private final NotificationMetrics notificationMetrics;
    private final String siteName;
    private final String mailFrom;

    public NotificationServiceImpl(
            JavaMailSender mailSender,
            NotificationLogRepository notificationLogRepository,
            NotificationLogMapper notificationLogMapper,
            UserLookupPort userLookup,
            Optional<NotificationIdempotencyService> idempotency,
            NotificationMetrics notificationMetrics,
            @Value("${app.notification.site-name}") String siteName,
            @Value("${app.notification.mail-from}") String mailFrom
    ) {
        this.mailSender = mailSender;
        this.notificationLogRepository = notificationLogRepository;
        this.notificationLogMapper = notificationLogMapper;
        this.userLookup = userLookup;
        this.idempotency = idempotency;
        this.notificationMetrics = notificationMetrics;
        this.siteName = siteName;
        this.mailFrom = mailFrom;
    }

    @Override
    public void sendEmailNotification(NotificationEmailRequest request) {
        if (idempotency.map(service -> service.isAlreadyProcessed(request.eventId())).orElse(false)) {
            notificationMetrics.duplicateSkipped();
            log.info("Пропуск дубликата уведомления: eventId={}", request.eventId());
            return;
        }
        userLookup.findByEmail(request.email())
                .ifPresent(view -> log.debug(
                        "Redis enrichment: user.id={}, cache.email={}, status={}",
                        view.id(), view.email(), view.status()));
        String body = buildBody(request);
        String subject = buildSubject(request.operation());
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setFrom(mailFrom);
            helper.setTo(request.email());
            helper.setSubject(subject);
            helper.setText(body, false);
            mailSender.send(message);

            notificationLogRepository.save(
                    notificationLogMapper.toEntity(request, NotificationDeliveryStatus.SENT, null));
            idempotency.ifPresent(service -> service.markProcessed(request.eventId()));
            notificationMetrics.emailSent(request.operation());
            log.info("Отправлено уведомление: operation={}, email={}", request.operation(), request.email());
        } catch (Exception e) {
            notificationMetrics.emailFailed(request.operation());
            notificationLogRepository.save(
                    notificationLogMapper.toEntity(request, NotificationDeliveryStatus.FAILED, truncate(e.getMessage())));
            log.error("Не удалось отправить уведомление: operation={}, email={}",
                    request.operation(), request.email(), e);
            throw new EmailDeliveryException("Не удалось отправить письмо", e);
        }
    }

    private String buildBody(NotificationEmailRequest request) {
        return switch (request.operation()) {
            case USER_CREATED -> "Здравствуйте! Ваш аккаунт на сайте " + siteName + " был успешно создан.";
            case USER_DELETED -> "Здравствуйте! Ваш аккаунт был удалён.";
        };
    }

    private String buildSubject(UserNotificationOperation operation) {
        return switch (operation) {
            case USER_CREATED -> "Аккаунт создан";
            case USER_DELETED -> "Аккаунт удалён";
        };
    }

    private static String truncate(String message) {
        if (message == null) {
            return null;
        }
        return message.length() <= MAX_ERROR_MESSAGE_LENGTH
                ? message
                : message.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }
}
