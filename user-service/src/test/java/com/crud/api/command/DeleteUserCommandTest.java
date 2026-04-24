package com.crud.api.command;

import com.crud.controller.UserController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteUserCommandTest extends ConsoleCommandTest {

    @Mock
    private UserController controller;

    @Test
    void execute_WhenConfirmed_ShouldDeleteUser() {
        provideInput("1\ny\n");
        DeleteUserCommand command = new DeleteUserCommand(controller, getScanner());
        doNothing().when(controller).deleteUser(1L);

        command.execute();

        verify(controller).deleteUser(1L);
        assertTrue(getOutput().contains("✅ Пользователь с ID 1 удалён"));
    }

    @Test
    void execute_WhenNotConfirmed_ShouldNotDelete() {
        provideInput("1\nn\n");
        DeleteUserCommand command = new DeleteUserCommand(controller, getScanner());
        command.execute();

        verify(controller, never()).deleteUser(anyLong());
        assertTrue(getOutput().contains("Удаление отменено"));
    }

    @Test
    void execute_WhenDeletionFails_ShouldPrintError() {
        provideInput("1\ny\n");
        DeleteUserCommand command = new DeleteUserCommand(controller, getScanner());
        doThrow(new RuntimeException("DB error")).when(controller).deleteUser(1L);

        command.execute();

        verify(controller).deleteUser(1L);
        assertTrue(getOutput().contains("❌ Ошибка удаления: DB error"));
    }
}