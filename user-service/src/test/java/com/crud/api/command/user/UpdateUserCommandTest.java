package com.crud.api.command.user;

import com.crud.api.command.ConsoleCommandTest;
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
    void execute_ShouldUpdateUserAndPrintSuccess() {
        provideInput("1\nNewName\nnew@example.com\n30\n");
        UpdateUserCommand command = new UpdateUserCommand(controller, getConsoleInput());
        when(controller.findUserById(1L))
                .thenReturn(new UserResponse(1L, "Old", "old@example.com", 20, LocalDateTime.now()));
        when(controller.updateUser(eq(1L), any(UserRequest.class)))
                .thenReturn(new UserResponse(1L, "NewName", "new@example.com", 30, LocalDateTime.now()));

        command.execute();

        verify(controller).updateUser(eq(1L), any(UserRequest.class));
    }

    @Test
    void execute_WhenUserNotFound_ShouldNotThrow() {
        provideInput("999\n");
        UpdateUserCommand command = new UpdateUserCommand(controller, getConsoleInput());
        doThrow(new RuntimeException("User not found")).when(controller).findUserById(999L);

        assertDoesNotThrow(command::execute);
    }
}
