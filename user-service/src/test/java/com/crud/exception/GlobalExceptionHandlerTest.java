package com.crud.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.crud.security.ApiOutputSanitizer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@Execution(ExecutionMode.CONCURRENT)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(
            mock(com.platform.commons.observability.ExceptionMetrics.class),
            new ApiOutputSanitizer());

    @Test
    void handleUserNotFound_ShouldReturn404() {
        UserNotFoundException ex = new UserNotFoundException(1L);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleUserNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        GlobalExceptionHandler.ErrorResponse body = response.getBody();
        Assertions.assertNotNull(body);
        assertThat(body.status()).isEqualTo(404);
        assertThat(body.message()).contains("1");
    }

    @Test
    void handleNoteNotFound_ShouldReturn404() {
        NoteNotFoundException ex = new NoteNotFoundException(5L);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleNoteNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        GlobalExceptionHandler.ErrorResponse body = response.getBody();
        Assertions.assertNotNull(body);
        assertThat(body.status()).isEqualTo(404);
    }

    @Test
    void handleRoleNotFound_ShouldReturn404() {
        RoleNotFoundException ex = new RoleNotFoundException(3L);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleRoleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        GlobalExceptionHandler.ErrorResponse body = response.getBody();
        Assertions.assertNotNull(body);
    }

    @Test
    void handleProfileNotFound_ShouldReturn404() {
        ProfileNotFoundException ex = new ProfileNotFoundException(10L);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleProfileNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        GlobalExceptionHandler.ErrorResponse body = response.getBody();
        Assertions.assertNotNull(body);
    }

    @Test
    void handleValidation_ShouldReturn400() {
        ValidationException ex = new ValidationException("Email уже используется");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        GlobalExceptionHandler.ErrorResponse body = response.getBody();
        Assertions.assertNotNull(body);
        assertThat(body.status()).isEqualTo(400);
        assertThat(body.message()).contains("Email уже используется");
    }

    @Test
    void handleConflict_ShouldReturn409() {
        UserServiceException ex = new UserServiceException("Роль с именем 'ADMIN' уже существует");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleConflict(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        GlobalExceptionHandler.ErrorResponse body = response.getBody();
        Assertions.assertNotNull(body);
        assertThat(body.status()).isEqualTo(409);
        assertThat(body.message()).contains("уже существует");
    }

    @Test
    void handleConflict_VersionConflict_ShouldReturn409() {
        UserServiceException ex = new UserServiceException("Конфликт версии при обновлении пользователя. Повторите операцию позже.");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleConflict(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        GlobalExceptionHandler.ErrorResponse body = response.getBody();
        Assertions.assertNotNull(body);
        assertThat(body.status()).isEqualTo(409);
        assertThat(body.message()).contains("Конфликт версии");
    }

    @Test
    void handleNoResourceFound_ShouldReturn404() {
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/api/notes", "No static resource");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleNoResourceFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        GlobalExceptionHandler.ErrorResponse body = response.getBody();
        Assertions.assertNotNull(body);
        assertThat(body.status()).isEqualTo(404);
        assertThat(body.message()).contains("Ресурс не найден");
    }

    @Test
    void handleMethodNotAllowed_ShouldReturn405() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("POST");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleMethodNotAllowed(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        GlobalExceptionHandler.ErrorResponse body = response.getBody();
        Assertions.assertNotNull(body);
        assertThat(body.status()).isEqualTo(405);
        assertThat(body.message()).contains("POST");
    }

    @Test
    void handleGeneric_ShouldReturn500WithoutMessage() {
        Exception ex = new RuntimeException("Database connection failed: secret_password");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleGeneric(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        GlobalExceptionHandler.ErrorResponse body = response.getBody();
        Assertions.assertNotNull(body);
        assertThat(body.status()).isEqualTo(500);
        assertThat(body.message()).doesNotContain("secret_password");
    }

    @Test
    void errorResponse_ShouldContainAllFields() {
        UserNotFoundException ex = new UserNotFoundException(1L);
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleUserNotFound(ex);
        GlobalExceptionHandler.ErrorResponse body = response.getBody();

        Assertions.assertNotNull(body);
        assertThat(body.timestamp()).isNotNull();
        assertThat(body.status()).isEqualTo(404);
        assertThat(body.error()).isEqualTo("Not Found");
        assertThat(body.message()).isNotNull();
    }
}
