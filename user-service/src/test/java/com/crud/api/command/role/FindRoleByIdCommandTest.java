package com.crud.api.command.role;

import com.crud.api.command.ConsoleCommandTest;
import com.crud.controller.RoleController;
import com.crud.dto.RoleResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindRoleByIdCommandTest extends ConsoleCommandTest {

    @Mock
    private RoleController roleController;

    @Test
    void execute_WhenRoleExists_ShouldCallController() {
        provideInput("1\n");
        FindRoleByIdCommand command = new FindRoleByIdCommand(roleController, getConsoleInput());
        when(roleController.findRoleById(1L))
                .thenReturn(new RoleResponse(1L, "Admin", LocalDateTime.now(), LocalDateTime.now()));

        command.execute();

        verify(roleController).findRoleById(1L);
    }

    @Test
    void execute_WhenControllerThrowsException_ShouldNotThrow() {
        provideInput("999\n");
        FindRoleByIdCommand command = new FindRoleByIdCommand(roleController, getConsoleInput());
        when(roleController.findRoleById(999L)).thenThrow(new RuntimeException("Role not found"));

        assertDoesNotThrow(command::execute);
    }
}
