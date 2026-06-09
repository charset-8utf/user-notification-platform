package com.crud.notification;

import com.crud.cache.UserCachePort;
import com.crud.entity.NotificationDeliveryStatus;
import com.crud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDeliveryFailureRecorder {

    private final UserRepository userRepository;
    private final UserCachePort userCache;

    @Transactional
    public void record(UserNotificationEvent event, Throwable cause) {
        userRepository.findByEmail(event.email()).ifPresent(user -> {
            user.setNotificationDeliveryStatus(NotificationDeliveryStatus.FAILED);
            userRepository.save(user);
            userCache.evict(user.getId());
            log.warn(
                    "Notification delivery failed for userId={}, eventId={}, cause={}",
                    user.getId(),
                    event.eventId(),
                    cause.toString());
        });
    }
}
