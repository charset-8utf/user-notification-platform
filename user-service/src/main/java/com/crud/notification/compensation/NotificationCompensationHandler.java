package com.crud.notification.compensation;

import com.crud.entity.NotificationDeliveryStatus;
import com.crud.entity.User;
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
    private final NotificationCompensationMetrics metrics;

    @Transactional
    public void handle(NotificationCompensationEvent event) {
        metrics.compensationReceived(event.originalOperation());
        userRepository.findByEmail(event.email()).ifPresentOrElse(
                user -> markFailed(user, event),
                () -> log.warn(
                        "Compensation: user not found for email={}, originalEventId={}",
                        event.email(),
                        event.originalEventId()));
    }

    private void markFailed(User user, NotificationCompensationEvent event) {
        user.setNotificationDeliveryStatus(NotificationDeliveryStatus.FAILED);
        userRepository.save(user);
        log.warn(
                "Compensation applied: userId={}, email={}, originalEventId={}, operation={}, error={}",
                user.getId(),
                event.email(),
                event.originalEventId(),
                event.originalOperation(),
                event.errorMessage());
    }
}
