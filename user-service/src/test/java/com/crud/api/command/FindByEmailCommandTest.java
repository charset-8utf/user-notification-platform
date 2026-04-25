package com.crud.api.command;

import com.crud.controller.UserController;
import com.crud.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindByEmailCommandTest extends ConsoleCommandTest {

    @Mock
    private UserController controller;

    @Test
    void execute_WhenUserExists_ShouldPrintUserDetails() {
        provideInput("test@example.com\n");
        FindByEmailCommand command = new FindByEmailCommand(controller, getScanner());
        UserResponse user = new UserResponse(1L, "John", "test@example.com", 30, LocalDateTime.now());
        when(controller.findUserByEmail("test@example.com")).thenReturn(user);

        command.execute();

        verify(controller).findUserByEmail("test@example.com");
        String output = getOutput();
        assertTrue(output.contains("🔍 Найден пользователь:"));
        assertTrue(output.contains("Email: test@example.com"));
        assertTrue(output.contains("Имя: John"));
    }

    @Test
    void execute_WhenUserNotFound_ShouldPrintError() {
        provideInput("missing@example.com\n");
        FindByEmailCommand command = new FindByEmailCommand(controller, getScanner());
        when(controller.findUserByEmail("missing@example.com")).thenThrow(new RuntimeException("Пользователь с email missing@example.com не найден"));

        command.execute();

        verify(controller).findUserByEmail("missing@example.com");
        assertTrue(getOutput().contains("❌ Ошибка: Пользователь с email missing@example.com не найден"));
    }
}