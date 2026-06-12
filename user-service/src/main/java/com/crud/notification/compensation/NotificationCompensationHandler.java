package com.crud.notification.compensation;

import com.crud.entity.User;
import com.crud.notification.UserNotificationOperation;
import com.crud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationCompensationHandler {

    private final UserRepository userRepository;
    private final UserCompensationService userCompensationService;
    private final NotificationCompensationMetrics metrics;

    @Transactional
    public void handle(NotificationCompensationEvent event) {
        metrics.compensationReceived(event.originalOperation());

        if (event.originalOperation() == UserNotificationOperation.USER_DELETED) {
            userCompensationService.signalDeleteNotificationUndelivered(event);
            return;
        }

        if (event.originalOperation() != UserNotificationOperation.USER_CREATED) {
            log.warn("Неподдерживаемая операция компенсации: {}", event.originalOperation());
            return;
        }

        userRepository.findByEmail(event.email()).ifPresentOrElse(
                user -> compensateUserCreated(user, event),
                () -> {
                    metrics.compensationIdempotent(event.originalOperation());
                    log.info(
                            "Компенсация идемпотентна: пользователь уже отсутствует — email={}, originalEventId={}",
                            event.email(),
                            event.originalEventId());
                });
    }

    private void compensateUserCreated(User user, NotificationCompensationEvent event) {
        userCompensationService.rollbackUserCreate(user, event);
    }
}
