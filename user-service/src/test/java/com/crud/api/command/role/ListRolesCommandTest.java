package com.crud.api.command.role;

import com.crud.api.command.ConsoleCommandTest;
import com.crud.controller.RoleController;
import com.crud.dto.Page;
import com.crud.dto.Pageable;
import com.crud.dto.RoleResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListRolesCommandTest extends ConsoleCommandTest {

    @Mock
    private RoleController roleController;

    @Test
    void execute_WhenRolesExist_ShouldCallController() {
        provideInput("0\n");
        ListRolesCommand command = new ListRolesCommand(roleController, getConsoleInput());

        RoleResponse role1 = new RoleResponse(1L, "Admin", LocalDateTime.now(), LocalDateTime.now());
        RoleResponse role2 = new RoleResponse(2L, "User", LocalDateTime.now(), LocalDateTime.now());
        Page<RoleResponse> page = new Page<>(List.of(role1, role2), 2, 0, 5);

        when(roleController.findAllRoles(any(Pageable.class))).thenReturn(page);

        command.execute();

        verify(roleController).findAllRoles(any(Pageable.class));
    }

    @Test
    void execute_WhenNoRoles_ShouldCallController() {
        provideInput("0\n");
        ListRolesCommand command = new ListRolesCommand(roleController, getConsoleInput());
        Page<RoleResponse> emptyPage = new Page<>(List.of(), 0, 0, 5);

        when(roleController.findAllRoles(any(Pageable.class))).thenReturn(emptyPage);

        command.execute();

        verify(roleController).findAllRoles(any(Pageable.class));
    }

    @Test
    void execute_WhenControllerThrowsException_ShouldNotThrow() {
        provideInput("0\n");
        ListRolesCommand command = new ListRolesCommand(roleController, getConsoleInput());
        when(roleController.findAllRoles(any(Pageable.class))).thenThrow(new RuntimeException("Database error"));

        assertDoesNotThrow(command::execute);
    }
}
