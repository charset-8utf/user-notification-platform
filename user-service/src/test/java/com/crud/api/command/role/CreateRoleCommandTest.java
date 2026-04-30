package com.crud.api.command.role;

import com.crud.api.command.ConsoleCommandTest;
import com.crud.controller.RoleController;
import com.crud.dto.RoleRequest;
import com.crud.dto.RoleResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateRoleCommandTest extends ConsoleCommandTest {

    @Mock
    private RoleController roleController;

    @Test
    void execute_ShouldCreateRole() {
        provideInput("1\nAdmin\n");
        CreateRoleCommand command = new CreateRoleCommand(roleController, getConsoleInput());
        when(roleController.createRole(any(RoleRequest.class)))
                .thenReturn(new RoleResponse(1L, "Admin", null, null));

        command.execute();

        verify(roleController).createRole(any(RoleRequest.class));
    }

    @Test
    void execute_WhenNameIsBlank_ShouldNotCallController() {
        provideInput("1\n\n");
        CreateRoleCommand command = new CreateRoleCommand(roleController, getConsoleInput());

        command.execute();

        verify(roleController, never()).createRole(any(RoleRequest.class));
    }

    @Test
    void execute_WhenControllerThrowsException_ShouldNotThrow() {
        provideInput("1\nAdmin\n");
        CreateRoleCommand command = new CreateRoleCommand(roleController, getConsoleInput());
        when(roleController.createRole(any(RoleRequest.class)))
                .thenThrow(new RuntimeException("Database error"));

        assertDoesNotThrow(command::execute);
    }
}
