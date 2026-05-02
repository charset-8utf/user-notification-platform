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
class DeleteRoleCommandTest extends ConsoleCommandTest {

    @Mock
    private RoleController roleController;

    @Test
    void execute_WhenConfirmed_ShouldDeleteRole() {
        provideInput("1\ny\n");
        DeleteRoleCommand command = new DeleteRoleCommand(roleController, getConsoleInput());
        doNothing().when(roleController).deleteRole(1L);

        command.execute();

        verify(roleController).deleteRole(1L);
    }

    @Test
    void execute_WhenNotConfirmed_ShouldNotDelete() {
        provideInput("1\nn\n");
        DeleteRoleCommand command = new DeleteRoleCommand(roleController, getConsoleInput());

        command.execute();

        verify(roleController, never()).deleteRole(anyLong());
    }

    @Test
    void execute_WhenControllerThrowsException_ShouldNotThrow() {
        provideInput("1\ny\n");
        DeleteRoleCommand command = new DeleteRoleCommand(roleController, getConsoleInput());
        doThrow(new RuntimeException("Role not found")).when(roleController).deleteRole(1L);

        assertDoesNotThrow(command::execute);
    }
}
