package com.notification;

import com.notification.controller.NotificationController;
import com.notification.entity.UserNotificationOperation;
import com.notification.exception.GlobalExceptionHandler;
import com.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationController.class)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("rest")
class NotificationControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void postEmailReturns204() throws Exception {
        mockMvc.perform(post("/api/notifications/email")
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
}
