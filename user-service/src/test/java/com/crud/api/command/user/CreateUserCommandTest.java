package com.crud.api.command.user;

import com.crud.api.command.ConsoleCommandTest;
import com.crud.controller.UserController;
import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateUserCommandTest extends ConsoleCommandTest {

    @Mock
    private UserController controller;

    @Test
    void execute_ShouldCreateUser() {
        provideInput("John\njohn@example.com\n30\n");
        CreateUserCommand command = new CreateUserCommand(controller, getConsoleInput());
        when(controller.createUser(any(UserRequest.class)))
                .thenReturn(new UserResponse(1L, "John", "john@example.com", 30, null));

        command.execute();

        verify(controller).createUser(any(UserRequest.class));
    }

    @Test
    void execute_WhenControllerThrowsException_ShouldNotThrow() {
        provideInput("John\njohn@example.com\n30\n");
        CreateUserCommand command = new CreateUserCommand(controller, getConsoleInput());
        when(controller.createUser(any(UserRequest.class)))
                .thenThrow(new RuntimeException("Database error"));

        assertDoesNotThrow(command::execute);
    }
}
