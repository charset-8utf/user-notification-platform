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
class FindUserCommandTest extends ConsoleCommandTest {

    @Mock
    private UserController controller;

    @Test
    void execute_WhenUserExists_ShouldPrintUser() {
        provideInput("1\n");
        FindUserCommand command = new FindUserCommand(controller, getConsoleInput());
        when(controller.findUserById(1L))
                .thenReturn(new UserResponse(1L, "John", "john@example.com", 30, LocalDateTime.now()));

        command.execute();

        verify(controller).findUserById(1L);
    }

    @Test
    void execute_WhenUserNotFound_ShouldNotThrow() {
        provideInput("999\n");
        FindUserCommand command = new FindUserCommand(controller, getConsoleInput());
        doThrow(new RuntimeException("User not found")).when(controller).findUserById(999L);

        assertDoesNotThrow(command::execute);
    }
}
