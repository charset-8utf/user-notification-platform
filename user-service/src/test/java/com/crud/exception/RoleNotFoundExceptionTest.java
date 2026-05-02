package com.crud.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleNotFoundExceptionTest {

    @Test
    void constructor_WithId_ShouldCreateExceptionWithMessage() {
        RoleNotFoundException exception = new RoleNotFoundException(1L);

        assertEquals("Роль с id 1 не найдена", exception.getMessage());
    }

    @Test
    void constructor_WithMessage_ShouldCreateException() {
        RoleNotFoundException exception = new RoleNotFoundException("Роль не существует");

        assertEquals("Роль не существует", exception.getMessage());
    }

    @Test
    void shouldBeUserServiceException() {
        RoleNotFoundException exception = new RoleNotFoundException(1L);

        assertInstanceOf(UserServiceException.class, exception);
    }
}
