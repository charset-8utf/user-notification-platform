package com.crud.api.command.role;

import com.crud.api.command.ConsoleCommandTest;
import com.crud.controller.RoleController;
import com.crud.dto.RoleRequest;
import com.crud.dto.RoleResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateRoleCommandTest extends ConsoleCommandTest {

    @Mock
    private RoleController roleController;

    @Test
    void execute_ShouldUpdateRole() {
        provideInput("1\nNewName\n");
        UpdateRoleCommand command = new UpdateRoleCommand(roleController, getConsoleInput());
        when(roleController.findRoleById(1L))
                .thenReturn(new RoleResponse(1L, "OldName", LocalDateTime.now(), LocalDateTime.now()));
        when(roleController.updateRole(eq(1L), any(RoleRequest.class)))
                .thenReturn(new RoleResponse(1L, "NewName", LocalDateTime.now(), LocalDateTime.now()));

        command.execute();

        verify(roleController).updateRole(eq(1L), any(RoleRequest.class));
    }

    @Test
    void execute_WhenBlankName_ShouldKeepExisting() {
        provideInput("1\n\n");
        UpdateRoleCommand command = new UpdateRoleCommand(roleController, getConsoleInput());
        when(roleController.findRoleById(1L))
                .thenReturn(new RoleResponse(1L, "ExistingName", LocalDateTime.now(), LocalDateTime.now()));
        when(roleController.updateRole(eq(1L), any(RoleRequest.class)))
                .thenReturn(new RoleResponse(1L, "ExistingName", LocalDateTime.now(), LocalDateTime.now()));

        command.execute();

        verify(roleController).updateRole(eq(1L), argThat(req -> req.name().equals("ExistingName")));
    }

    @Test
    void execute_WhenControllerThrowsException_ShouldNotThrow() {
        provideInput("999\n");
        UpdateRoleCommand command = new UpdateRoleCommand(roleController, getConsoleInput());
        when(roleController.findRoleById(999L)).thenThrow(new RuntimeException("Role not found"));

        assertDoesNotThrow(command::execute);
    }
}
