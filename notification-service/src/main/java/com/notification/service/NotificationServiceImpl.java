package com.notification.service;

import com.notification.dto.NotificationEmailRequest;
import com.notification.entity.NotificationDeliveryStatus;
import com.notification.entity.UserNotificationOperation;
import com.notification.exception.EmailDeliveryException;
import com.notification.idempotency.NotificationIdempotencyService;
import com.notification.lookup.UserLookupPort;
import com.notification.mapper.NotificationLogMapper;
import com.notification.repository.NotificationLogRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Реализация сервиса уведомлений.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 2000;

    private final JavaMailSender mailSender;
    private final NotificationLogRepository notificationLogRepository;
    private final NotificationLogMapper notificationLogMapper;
    private final UserLookupPort userLookup;

    @Autowired(required = false)
    private NotificationIdempotencyService idempotency;

    @Value("${app.notification.site-name}")
    private String siteName;

    @Value("${app.notification.mail-from}")
    private String mailFrom;

    @Override
    public void sendEmailNotification(NotificationEmailRequest request) {
        if (idempotency != null && idempotency.isAlreadyProcessed(request.eventId())) {
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
            if (idempotency != null) {
                idempotency.markProcessed(request.eventId());
            }
            log.info("Отправлено уведомление: operation={}, email={}", request.operation(), request.email());
        } catch (Exception e) {
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
