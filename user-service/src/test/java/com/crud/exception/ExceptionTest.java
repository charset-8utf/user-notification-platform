package com.crud.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.assertj.core.api.Assertions.assertThat;

@Execution(ExecutionMode.CONCURRENT)
class ExceptionTest {

    @Test
    void userServiceException_WithMessage_ShouldContainMessage() {
        UserServiceException ex = new UserServiceException("test message");
        assertThat(ex.getMessage()).isEqualTo("test message");
    }

    @Test
    void userServiceException_WithMessageAndCause_ShouldContainBoth() {
        RuntimeException cause = new RuntimeException("root cause");
        UserServiceException ex = new UserServiceException("test message", cause);
        assertThat(ex.getMessage()).isEqualTo("test message");
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    void userNotFoundException_WithId_ShouldContainIdInMessage() {
        UserNotFoundException ex = new UserNotFoundException(42L);
        assertThat(ex.getMessage()).contains("42");
    }

    @Test
    void userNotFoundException_WithStringMessage_ShouldContainMessage() {
        UserNotFoundException ex = new UserNotFoundException("custom message");
        assertThat(ex.getMessage()).isEqualTo("custom message");
    }

    @Test
    void userNotFoundException_ShouldExtendUserServiceException() {
        assertThat(UserServiceException.class).isAssignableFrom(UserNotFoundException.class);
    }

    @Test
    void noteNotFoundException_WithId_ShouldContainIdInMessage() {
        NoteNotFoundException ex = new NoteNotFoundException(5L);
        assertThat(ex.getMessage()).contains("5");
    }

    @Test
    void profileNotFoundException_WithUserId_ShouldContainUserIdInMessage() {
        ProfileNotFoundException ex = new ProfileNotFoundException(10L);
        assertThat(ex.getMessage()).contains("10");
    }

    @Test
    void roleNotFoundException_WithId_ShouldContainIdInMessage() {
        RoleNotFoundException ex = new RoleNotFoundException(3L);
        assertThat(ex.getMessage()).contains("3");
    }

    @Test
    void validationException_WithMessage_ShouldContainMessage() {
        ValidationException ex = new ValidationException("email уже используется");
        assertThat(ex.getMessage()).isEqualTo("email уже используется");
    }

    @Test
    void validationException_WithMessageAndCause_ShouldContainBoth() {
        RuntimeException cause = new RuntimeException("cause");
        ValidationException ex = new ValidationException("validation failed", cause);
        assertThat(ex.getMessage()).isEqualTo("validation failed");
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    void kafkaSecurityConfigurationException_ShouldExtendUserServiceException() {
        assertThat(UserServiceException.class).isAssignableFrom(KafkaSecurityConfigurationException.class);
    }
}
