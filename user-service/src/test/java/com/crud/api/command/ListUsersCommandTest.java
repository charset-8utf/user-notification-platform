package com.crud.api.command;

import com.crud.controller.UserController;
import com.crud.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListUsersCommandTest {

    @Mock
    private UserController controller;

    @Test
    void execute_WhenUsersExist_ShouldPrintList() {
        List<UserResponse> users = List.of(
                new UserResponse(1L, "John", "john@ex.com", 30, LocalDateTime.now()),
                new UserResponse(2L, "Jane", "jane@ex.com", 25, LocalDateTime.now())
        );
        when(controller.findAllUsers()).thenReturn(users);

        var out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        ListUsersCommand command = new ListUsersCommand(controller);
        command.execute();
        System.setOut(System.out);

        String output = out.toString();
        assertTrue(output.contains("👥 Список пользователей:"));
        assertTrue(output.contains("John"));
        assertTrue(output.contains("Jane"));
    }

    @Test
    void execute_WhenNoUsers_ShouldPrintEmptyMessage() {
        when(controller.findAllUsers()).thenReturn(List.of());

        var out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        ListUsersCommand command = new ListUsersCommand(controller);
        command.execute();
        System.setOut(System.out);

        assertTrue(out.toString().contains("📭 Список пользователей пуст"));
    }
}