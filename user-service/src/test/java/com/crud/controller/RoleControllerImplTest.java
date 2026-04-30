package com.crud.controller;

import com.crud.dto.RoleRequest;
import com.crud.dto.RoleResponse;
import com.crud.service.RoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleControllerImplTest {

    @Mock
    private RoleService roleService;

    @InjectMocks
    private RoleControllerImpl roleController;

    @Test
    void createRole_ShouldDelegateToService() {
        RoleRequest request = new RoleRequest(1L, "ADMIN");
        RoleResponse expected = new RoleResponse(1L, "ADMIN", LocalDateTime.now(), LocalDateTime.now());
        when(roleService.createRole(request)).thenReturn(expected);

        RoleResponse actual = roleController.createRole(request);

        assertEquals(expected, actual);
        verify(roleService).createRole(request);
    }

    @Test
    void findRoleById_ShouldDelegateToService() {
        RoleResponse expected = new RoleResponse(1L, "USER", LocalDateTime.now(), LocalDateTime.now());
        when(roleService.findRoleById(1L)).thenReturn(expected);

        RoleResponse actual = roleController.findRoleById(1L);

        assertEquals(expected, actual);
        verify(roleService).findRoleById(1L);
    }

    @Test
    void updateRole_ShouldDelegateToService() {
        RoleRequest request = new RoleRequest(null, "Moderator");
        RoleResponse expected = new RoleResponse(1L, "Moderator", LocalDateTime.now(), LocalDateTime.now());
        when(roleService.updateRole(1L, request)).thenReturn(expected);

        RoleResponse actual = roleController.updateRole(1L, request);

        assertEquals(expected, actual);
        verify(roleService).updateRole(1L, request);
    }

    @Test
    void deleteRole_ShouldDelegateToService() {
        doNothing().when(roleService).deleteRole(1L);

        roleController.deleteRole(1L);

        verify(roleService).deleteRole(1L);
    }

    @Test
    void assignRoleToUser_ShouldDelegateToService() {
        doNothing().when(roleService).assignRoleToUser(1L, 2L);

        roleController.assignRoleToUser(1L, 2L);

        verify(roleService).assignRoleToUser(1L, 2L);
    }

    @Test
    void removeRoleFromUser_ShouldDelegateToService() {
        doNothing().when(roleService).removeRoleFromUser(1L, 2L);

        roleController.removeRoleFromUser(1L, 2L);

        verify(roleService).removeRoleFromUser(1L, 2L);
    }
}
