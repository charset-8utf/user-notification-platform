package com.notification.controller;

import com.notification.config.SecurityConfig;
import com.notification.exception.GlobalExceptionHandler;
import com.notification.dto.NotificationLogSummaryResponse;
import com.notification.security.JwtRoleSupport;
import com.notification.security.NotificationLogAccessPolicy;
import com.notification.security.SecurityJsonErrorWriter;
import com.notification.security.ServiceJwtAudienceValidator;
import com.notification.security.ServiceJwtAuthorities;
import com.notification.security.ServiceJwtSecurityConfig;
import com.notification.security.UserJwtSecurityConfig;
import com.notification.service.NotificationLogQueryService;
import com.notification.support.UserJwtTestSupport;
import com.platform.commons.observability.ExceptionMetrics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationLogController.class)
@Import({
        GlobalExceptionHandler.class,
        SecurityConfig.class,
        ServiceJwtSecurityConfig.class,
        UserJwtSecurityConfig.class,
        JwtRoleSupport.class,
        NotificationLogAccessPolicy.class,
        ServiceJwtAudienceValidator.class,
        ServiceJwtAuthorities.class,
        SecurityJsonErrorWriter.class
})
@ActiveProfiles("rest")
@TestPropertySource(properties = {
        "app.security.jwt.secret=test-jwt-secret-for-notification-read-min-32b",
        "app.security.service-jwt.secret=test-service-jwt-secret-for-tests-min-32b"
})
class NotificationLogControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationLogQueryService notificationLogQueryService;

    @MockitoBean
    private ExceptionMetrics exceptionMetrics;

    @Test
    void latest_withMatchingEmail_returnsSummary() throws Exception {
        when(notificationLogQueryService.latestByEmail(eq("user@example.com")))
                .thenReturn(new NotificationLogSummaryResponse(
                        true,
                        "USER_CREATED",
                        "EMAIL",
                        "SENT",
                        "user@example.com",
                        LocalDateTime.parse("2026-05-30T10:00:00"),
                        "OK"));

        mockMvc.perform(get("/api/notifications/logs/latest")
                        .param("email", "user@example.com")
                        .header(HttpHeaders.AUTHORIZATION, UserJwtTestSupport.bearerToken("user@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.found").value(true))
                .andExpect(jsonPath("$.status").value("SENT"));
    }

    @Test
    void latest_withForeignEmail_returns403() throws Exception {
        mockMvc.perform(get("/api/notifications/logs/latest")
                        .param("email", "other@example.com")
                        .header(HttpHeaders.AUTHORIZATION, UserJwtTestSupport.bearerToken("user@example.com")))
                .andExpect(status().isForbidden());
    }
}
