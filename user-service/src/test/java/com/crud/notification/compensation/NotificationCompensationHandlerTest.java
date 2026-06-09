package com.crud.notification.compensation;

import com.crud.cache.UserCachePort;
import com.crud.entity.NotificationDeliveryStatus;
import com.crud.entity.User;
import com.crud.notification.UserNotificationOperation;
import com.crud.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationCompensationHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCachePort userCache;

    @Mock
    private NotificationCompensationMetrics metrics;

    @InjectMocks
    private NotificationCompensationHandler handler;

    @Test
    void handle_marksUserAsFailed() {
        User user = User.builder().email("u@example.com").name("U").age(30).build();
        user.setId(1L);
        when(userRepository.findByEmail("u@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationCompensationEvent event = new NotificationCompensationEvent(
                UUID.randomUUID(),
                UserNotificationOperation.USER_CREATED,
                "u@example.com",
                "smtp timeout",
                Instant.now());

        handler.handle(event);

        assertThat(user.getNotificationDeliveryStatus()).isEqualTo(NotificationDeliveryStatus.FAILED);
        verify(userCache).evict(user.getId());
        verify(metrics).compensationReceived(UserNotificationOperation.USER_CREATED);
    }

    @Test
    void handle_unknownEmail_doesNotSave() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        handler.handle(new NotificationCompensationEvent(
                UUID.randomUUID(),
                UserNotificationOperation.USER_CREATED,
                "missing@example.com",
                "err",
                Instant.now()));

        verify(userRepository, never()).save(any());
    }
}
