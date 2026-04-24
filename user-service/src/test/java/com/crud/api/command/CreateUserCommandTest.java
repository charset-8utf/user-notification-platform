package com.crud.api.command;

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
    void execute_ShouldCreateUserAndPrintSuccess() {
        provideInput("John\njohn@example.com\n30\n");
        CreateUserCommand command = new CreateUserCommand(controller, getScanner());
        when(controller.createUser(any(UserRequest.class)))
                .thenReturn(new UserResponse(1L, "John", "john@example.com", 30, null));

        command.execute();

        verify(controller).createUser(any(UserRequest.class));
        assertTrue(getOutput().contains("✅ Пользователь создан! ID: 1"));
    }

    @Test
    void execute_WhenControllerThrowsException_ShouldPrintError() {
        provideInput("John\njohn@example.com\n30\n");
        CreateUserCommand command = new CreateUserCommand(controller, getScanner());
        when(controller.createUser(any(UserRequest.class)))
                .thenThrow(new RuntimeException("Database error"));

        command.execute();

        assertTrue(getOutput().contains("❌ Ошибка: Database error"));
    }
}