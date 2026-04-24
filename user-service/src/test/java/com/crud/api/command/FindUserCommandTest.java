package com.crud.api.command;

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
    void execute_WhenUserExists_ShouldPrintUserDetails() {
        provideInput("1\n");
        FindUserCommand command = new FindUserCommand(controller, getScanner());
        UserResponse user = new UserResponse(1L, "John", "john@example.com", 30, LocalDateTime.now());
        when(controller.findUserById(1L)).thenReturn(user);

        command.execute();

        verify(controller).findUserById(1L);
        String output = getOutput();
        assertTrue(output.contains("🔍 Найден пользователь:"));
        assertTrue(output.contains("ID: 1"));
        assertTrue(output.contains("Имя: John"));
    }

    @Test
    void execute_WhenUserNotFound_ShouldPrintError() {
        provideInput("999\n");
        FindUserCommand command = new FindUserCommand(controller, getScanner());
        when(controller.findUserById(999L)).thenThrow(new RuntimeException("User not found"));

        command.execute();

        verify(controller).findUserById(999L);
        assertTrue(getOutput().contains("❌ Ошибка: User not found"));
    }
}