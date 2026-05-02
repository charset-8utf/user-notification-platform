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
class AssignRoleToUserCommandTest extends ConsoleCommandTest {

    @Mock
    private RoleController roleController;

    @Test
    void execute_ShouldAssignRoleToUser() {
        provideInput("1\n2\n");
        AssignRoleToUserCommand command = new AssignRoleToUserCommand(roleController, getConsoleInput());
        doNothing().when(roleController).assignRoleToUser(1L, 2L);

        command.execute();

        verify(roleController).assignRoleToUser(1L, 2L);
    }

    @Test
    void execute_WhenControllerThrowsException_ShouldNotThrow() {
        provideInput("1\n2\n");
        AssignRoleToUserCommand command = new AssignRoleToUserCommand(roleController, getConsoleInput());
        doThrow(new RuntimeException("User not found")).when(roleController).assignRoleToUser(1L, 2L);

        assertDoesNotThrow(command::execute);
    }
}
