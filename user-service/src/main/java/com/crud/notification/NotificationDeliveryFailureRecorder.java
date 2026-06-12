package com.crud.notification;

import com.crud.notification.compensation.NotificationCompensationEvent;
import com.crud.notification.compensation.UserCompensationService;
import com.crud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Синхронный REST-путь (профиль {@code rest}): при сбое доставки — та же saga compensation, что и из Kafka.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDeliveryFailureRecorder {

    private final UserRepository userRepository;
    private final UserCompensationService userCompensationService;
    private final ThrowableMessageTruncator messageTruncator;

    @Transactional
    public void recordFailure(UserNotificationEvent event, Throwable cause) {
        NotificationCompensationEvent compensation = new NotificationCompensationEvent(
                event.eventId(),
                event.operation(),
                event.email(),
                messageTruncator.truncate(cause),
                Instant.now());

        if (event.operation() == UserNotificationOperation.USER_DELETED) {
            userCompensationService.signalDeleteNotificationUndelivered(compensation);
            return;
        }

        userRepository.findByEmail(event.email()).ifPresent(user ->
                userCompensationService.rollbackUserCreate(user, compensation));
    }
}
