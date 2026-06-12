package com.notification.service;

import com.notification.domain.NotificationChannel;
import com.notification.domain.NotificationDeliveryStatus;
import com.notification.domain.UserNotificationOperation;
import com.notification.dto.NotificationLogSummaryResponse;
import com.notification.entity.NotificationLog;
import com.notification.mapper.NotificationLogMapper;
import com.notification.repository.NotificationLogRepository;
import com.notification.security.NotificationLogAccessPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationLogQueryServiceTest {

    @Mock
    private NotificationLogRepository notificationLogRepository;

    @Mock
    private NotificationLogMapper notificationLogMapper;

    @Mock
    private NotificationLogAccessPolicy notificationLogAccessPolicy;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private NotificationLogQueryService service;

    @Test
    void latestByEmail_checksAccessBeforeQuery() {
        NotificationLog log = NotificationLog.builder()
                .operation(UserNotificationOperation.USER_CREATED)
                .email("user@example.com")
                .channel(NotificationChannel.EMAIL)
                .status(NotificationDeliveryStatus.SENT)
                .createdAt(LocalDateTime.parse("2026-05-30T10:00:00"))
                .build();
        NotificationLogSummaryResponse summary = new NotificationLogSummaryResponse(
                true, "USER_CREATED", "EMAIL", "SENT",
                "user@example.com", log.getCreatedAt(), "OK");

        when(notificationLogRepository.findFirstByEmailOrderByCreatedAtDesc("user@example.com"))
                .thenReturn(Optional.of(log));
        when(notificationLogMapper.toSummary(log)).thenReturn(summary);

        NotificationLogSummaryResponse result = service.latestByEmail("user@example.com", jwt);

        assertThat(result).isEqualTo(summary);
        verify(notificationLogAccessPolicy).assertCanRead("user@example.com", jwt);
    }

    @Test
    void latestByEmail_deniesAccessWithoutRepositoryCall() {
        doThrow(new AccessDeniedException("denied"))
                .when(notificationLogAccessPolicy).assertCanRead("other@example.com", jwt);

        assertThatThrownBy(() -> service.latestByEmail("other@example.com", jwt))
                .isInstanceOf(AccessDeniedException.class);

        verify(notificationLogRepository, never()).findFirstByEmailOrderByCreatedAtDesc("other@example.com");
    }
}
