package com.crud.api.command.user;

import com.crud.api.command.ConsoleCommandTest;
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
        DeleteUserCommand command = new DeleteUserCommand(controller, getConsoleInput());
        doNothing().when(controller).deleteUser(1L);

        command.execute();

        verify(controller).deleteUser(1L);
    }

    @Test
    void execute_WhenNotConfirmed_ShouldNotDelete() {
        provideInput("1\nn\n");
        DeleteUserCommand command = new DeleteUserCommand(controller, getConsoleInput());

        command.execute();

        verify(controller, never()).deleteUser(anyLong());
    }

    @Test
    void execute_WhenControllerThrowsException_ShouldNotThrow() {
        provideInput("1\ny\n");
        DeleteUserCommand command = new DeleteUserCommand(controller, getConsoleInput());
        doThrow(new RuntimeException("User not found")).when(controller).deleteUser(1L);

        assertDoesNotThrow(command::execute);
    }
}
