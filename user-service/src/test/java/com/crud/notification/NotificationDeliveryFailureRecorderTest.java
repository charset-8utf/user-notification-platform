package com.crud.notification;

import com.crud.entity.User;
import com.crud.notification.compensation.UserCompensationService;
import com.crud.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationDeliveryFailureRecorderTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCompensationService userCompensationService;

    @Mock
    private ThrowableMessageTruncator messageTruncator;

    @InjectMocks
    private NotificationDeliveryFailureRecorder recorder;

    @org.junit.jupiter.api.BeforeEach
    void stubTruncator() {
        lenient().when(messageTruncator.truncate(any(Throwable.class)))
                .thenAnswer(invocation -> {
                    Throwable cause = invocation.getArgument(0);
                    String message = cause.getMessage();
                    return message != null ? message : cause.getClass().getSimpleName();
                });
    }

    @Test
    void recordFailure_userCreated_rollsBack() {
        User user = User.builder().name("U").email("u@example.com").age(20).build();
        user.setId(2L);
        when(userRepository.findByEmail("u@example.com")).thenReturn(Optional.of(user));

        UserNotificationEvent event = UserNotificationEvent.create(
                UserNotificationOperation.USER_CREATED, "u@example.com");

        recorder.recordFailure(event, new RuntimeException("timeout"));

        verify(userCompensationService).rollbackUserCreate(any(), any());
    }

    @Test
    void recordFailure_userDeleted_signalsOnly() {
        UserNotificationEvent event = UserNotificationEvent.create(
                UserNotificationOperation.USER_DELETED, "gone@example.com");

        recorder.recordFailure(event, new RuntimeException("timeout"));

        verify(userCompensationService).signalDeleteNotificationUndelivered(any());
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void recordFailure_unknownEmail_doesNothing() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        recorder.recordFailure(
                new UserNotificationEvent(UUID.randomUUID(), UserNotificationOperation.USER_CREATED, "missing@example.com"),
                new RuntimeException("timeout"));

        verify(userCompensationService, never()).rollbackUserCreate(any(), any());
    }
}
