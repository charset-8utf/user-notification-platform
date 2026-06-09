package com.crud.notification;

import com.crud.cache.UserCachePort;
import com.crud.entity.NotificationDeliveryStatus;
import com.crud.entity.User;
import com.crud.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationDeliveryFailureRecorderTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCachePort userCache;

    @InjectMocks
    private NotificationDeliveryFailureRecorder recorder;

    @Test
    void record_marksUserFailed() {
        User user = User.builder().name("U").email("u@example.com").age(20).build();
        user.setId(2L);
        when(userRepository.findByEmail("u@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserNotificationEvent event = UserNotificationEvent.create(
                UserNotificationOperation.USER_CREATED, "u@example.com");

        recorder.record(event, new RuntimeException("timeout"));

        assertThat(user.getNotificationDeliveryStatus()).isEqualTo(NotificationDeliveryStatus.FAILED);
        verify(userRepository).save(user);
        verify(userCache).evict(2L);
    }

    @Test
    void record_unknownEmail_doesNothing() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        recorder.record(
                new UserNotificationEvent(UUID.randomUUID(), UserNotificationOperation.USER_CREATED, "missing@example.com"),
                new RuntimeException("timeout"));

        verify(userRepository, never()).save(any());
    }
}
