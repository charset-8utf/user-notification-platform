package com.crud.notification.compensation;

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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationCompensationHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCompensationService userCompensationService;

    @Mock
    private NotificationCompensationMetrics metrics;

    @InjectMocks
    private NotificationCompensationHandler handler;

    @Test
    void handle_userCreated_rollsBackUser() {
        User user = User.builder().email("u@example.com").name("U").age(30).build();
        user.setId(1L);
        when(userRepository.findByEmail("u@example.com")).thenReturn(Optional.of(user));

        NotificationCompensationEvent event = new NotificationCompensationEvent(
                UUID.randomUUID(),
                UserNotificationOperation.USER_CREATED,
                "u@example.com",
                "smtp timeout",
                Instant.now());

        handler.handle(event);

        verify(userCompensationService).rollbackUserCreate(user, event);
        verify(metrics).compensationReceived(UserNotificationOperation.USER_CREATED);
    }

    @Test
    void handle_userCreated_unknownEmail_idempotent() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        handler.handle(new NotificationCompensationEvent(
                UUID.randomUUID(),
                UserNotificationOperation.USER_CREATED,
                "missing@example.com",
                "err",
                Instant.now()));

        verify(userCompensationService, never()).rollbackUserCreate(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(metrics).compensationIdempotent(UserNotificationOperation.USER_CREATED);
    }

    @Test
    void handle_userDeleted_signalsOnly() {
        NotificationCompensationEvent event = new NotificationCompensationEvent(
                UUID.randomUUID(),
                UserNotificationOperation.USER_DELETED,
                "gone@example.com",
                "smtp down",
                Instant.now());

        handler.handle(event);

        verify(userCompensationService).signalDeleteNotificationUndelivered(event);
        verify(userRepository, never()).findByEmail(org.mockito.ArgumentMatchers.anyString());
    }
}
