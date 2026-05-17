package com.notification.controller;

import com.notification.config.SecurityConfig;
import com.notification.entity.UserNotificationOperation;
import com.notification.exception.GlobalExceptionHandler;
import com.notification.security.ServiceJwtSecurityConfig;
import com.notification.service.NotificationService;
import com.notification.support.ServiceJwtTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, ServiceJwtSecurityConfig.class})
@ActiveProfiles("rest")
@TestPropertySource(properties = "app.security.service-jwt.secret=test-service-jwt-secret-for-tests-min-32b")
class NotificationControllerWebMvcTest {

    private static final String AUTH_HEADER = HttpHeaders.AUTHORIZATION;
    private static final String BEARER = ServiceJwtTestSupport.bearerToken();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void postEmailReturns204() throws Exception {
        mockMvc.perform(post("/api/notifications/email")
                        .header(AUTH_HEADER, BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventId": "550e8400-e29b-41d4-a716-446655440000",
                                  "operation": "USER_CREATED",
                                  "email": "user@example.com"
                                }
                                """))
                .andExpect(status().isNoContent());

        verify(notificationService).sendEmailNotification(argThat(r ->
                r.operation() == UserNotificationOperation.USER_CREATED
                        && "user@example.com".equals(r.email())
                        && r.eventId() != null));
    }

    @Test
    void postEmailInvalidReturns400() throws Exception {
        mockMvc.perform(post("/api/notifications/email")
                        .header(AUTH_HEADER, BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventId": "550e8400-e29b-41d4-a716-446655440001",
                                  "operation": "USER_CREATED",
                                  "email": "not-an-email"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("Ошибка валидации")));
    }

    @Test
    void postEmailWithoutTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validEmailBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postEmailWithWrongTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/notifications/email")
                        .header(AUTH_HEADER, "Bearer not-a-jwt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validEmailBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postEmailWithEmptyBearerReturns401() throws Exception {
        mockMvc.perform(post("/api/notifications/email")
                        .header(AUTH_HEADER, "Bearer ")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validEmailBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postEmailWithUserAccessTokenReturns401() throws Exception {
        String userJwt = ServiceJwtTestSupport.accessToken(
                ServiceJwtTestSupport.TEST_SECRET,
                "user-service",
                "admin",
                "notification-service",
                "ROLE_USER",
                java.time.Duration.ofMinutes(5));
        mockMvc.perform(post("/api/notifications/email")
                        .header(AUTH_HEADER, "Bearer " + userJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validEmailBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postEmailWithServiceJwtWrongScopeReturns403() throws Exception {
        String wrongScope = ServiceJwtTestSupport.accessToken(
                ServiceJwtTestSupport.TEST_SECRET,
                ServiceJwtTestSupport.ISSUER,
                ServiceJwtTestSupport.SUBJECT,
                ServiceJwtTestSupport.AUDIENCE,
                "other:scope",
                java.time.Duration.ofMinutes(5));
        mockMvc.perform(post("/api/notifications/email")
                        .header(AUTH_HEADER, "Bearer " + wrongScope)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validEmailBody()))
                .andExpect(status().isForbidden());
    }

    private static String validEmailBody() {
        return """
                {
                  "eventId": "550e8400-e29b-41d4-a716-446655440000",
                  "operation": "USER_CREATED",
                  "email": "user@example.com"
                }
                """;
    }
}
