package com.crud.notification.compensation;

import com.crud.cache.UserCachePort;
import com.crud.entity.User;
import com.crud.notification.UserNotificationOperation;
import com.crud.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserCompensationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCachePort userCache;

    @Mock
    private NotificationCompensationMetrics metrics;

    @InjectMocks
    private UserCompensationService service;

    @Test
    void rollbackUserCreate_deletesUserAndEvictsCache() {
        User user = User.builder().email("u@example.com").name("U").age(30).build();
        user.setId(42L);
        NotificationCompensationEvent event = new NotificationCompensationEvent(
                UUID.randomUUID(),
                UserNotificationOperation.USER_CREATED,
                "u@example.com",
                "fail",
                Instant.now());

        service.rollbackUserCreate(user, event);

        verify(userRepository).delete(user);
        verify(userCache).evict(42L);
        verify(metrics).compensationApplied(UserNotificationOperation.USER_CREATED, "ROLLBACK_DELETE");
    }

    @Test
    void signalDeleteNotificationUndelivered_recordsMetric() {
        NotificationCompensationEvent event = new NotificationCompensationEvent(
                UUID.randomUUID(),
                UserNotificationOperation.USER_DELETED,
                "gone@example.com",
                "fail",
                Instant.now());

        service.signalDeleteNotificationUndelivered(event);

        verify(metrics).compensationApplied(UserNotificationOperation.USER_DELETED, "SIGNAL_ONLY");
    }
}
