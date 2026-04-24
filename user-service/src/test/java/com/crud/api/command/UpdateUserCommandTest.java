package com.crud.api.command;

import com.crud.controller.UserController;
import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateUserCommandTest extends ConsoleCommandTest {

    @Mock
    private UserController controller;

    @Test
    void execute_ShouldUpdateUserWhenFound() {
        provideInput("1\nNew\nnew@example.com\n30\n");
        UpdateUserCommand command = new UpdateUserCommand(controller, getScanner());
        UserResponse existing = new UserResponse(1L, "Old", "old@example.com", 25, LocalDateTime.now());
        when(controller.findUserById(1L)).thenReturn(existing);
        when(controller.updateUser(eq(1L), any(UserRequest.class)))
                .thenReturn(new UserResponse(1L, "New", "new@example.com", 30, LocalDateTime.now()));

        command.execute();

        verify(controller).findUserById(1L);
        verify(controller).updateUser(eq(1L), any(UserRequest.class));
        assertTrue(getOutput().contains("✅ Пользователь с ID 1 обновлён"));
    }

    @Test
    void execute_WhenUserNotFound_ShouldPrintError() {
        provideInput("999\n");
        UpdateUserCommand command = new UpdateUserCommand(controller, getScanner());
        when(controller.findUserById(999L)).thenThrow(new RuntimeException("User not found"));

        command.execute();

        verify(controller, never()).updateUser(anyLong(), any());
        assertTrue(getOutput().contains("❌ Пользователь не найден: User not found"));
    }
}