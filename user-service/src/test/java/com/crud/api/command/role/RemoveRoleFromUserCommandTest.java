package com.crud.api.command.role;

import com.crud.api.command.ConsoleCommandTest;
import com.crud.controller.RoleController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemoveRoleFromUserCommandTest extends ConsoleCommandTest {

    @Mock
    private RoleController roleController;

    @Test
    void execute_WhenConfirmed_ShouldRemoveRoleFromUser() {
        provideInput("1\n2\ny\n");
        RemoveRoleFromUserCommand command = new RemoveRoleFromUserCommand(roleController, getConsoleInput());
        doNothing().when(roleController).removeRoleFromUser(1L, 2L);

        command.execute();

        verify(roleController).removeRoleFromUser(1L, 2L);
    }

    @Test
    void execute_WhenNotConfirmed_ShouldNotRemove() {
        provideInput("1\n2\nn\n");
        RemoveRoleFromUserCommand command = new RemoveRoleFromUserCommand(roleController, getConsoleInput());

        command.execute();

        verify(roleController, never()).removeRoleFromUser(anyLong(), anyLong());
    }

    @Test
    void execute_WhenControllerThrowsException_ShouldNotThrow() {
        provideInput("1\n2\ny\n");
        RemoveRoleFromUserCommand command = new RemoveRoleFromUserCommand(roleController, getConsoleInput());
        doThrow(new RuntimeException("Role not found")).when(roleController).removeRoleFromUser(1L, 2L);

        assertDoesNotThrow(command::execute);
    }
}
