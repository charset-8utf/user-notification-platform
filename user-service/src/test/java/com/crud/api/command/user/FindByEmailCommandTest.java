package com.crud.api.command.user;

import com.crud.api.command.ConsoleCommandTest;
import com.crud.controller.UserController;
import com.crud.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindByEmailCommandTest extends ConsoleCommandTest {

    @Mock
    private UserController controller;

    @Test
    void execute_WhenUserExists_ShouldPrintUser() {
        provideInput("test@example.com\n");
        FindByEmailCommand command = new FindByEmailCommand(controller, getConsoleInput());
        when(controller.findUserByEmail("test@example.com"))
                .thenReturn(new UserResponse(1L, "Test", "test@example.com", 25, LocalDateTime.now()));

        command.execute();

        verify(controller).findUserByEmail("test@example.com");
    }

    @Test
    void execute_WhenUserNotFound_ShouldNotThrow() {
        provideInput("missing@example.com\n");
        FindByEmailCommand command = new FindByEmailCommand(controller, getConsoleInput());
        doThrow(new RuntimeException("User not found")).when(controller).findUserByEmail("missing@example.com");

        assertDoesNotThrow(command::execute);
    }
}
