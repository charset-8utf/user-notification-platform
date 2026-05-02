package com.crud.controller;

import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import com.crud.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerImplTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserControllerImpl userController;

    @Test
    void createUser_ShouldDelegateToService() {
        UserRequest request = new UserRequest("John", "john@example.com", 30);
        UserResponse expected = new UserResponse(1L, "John", "john@example.com", 30, LocalDateTime.now());
        when(userService.createUser(request)).thenReturn(expected);

        UserResponse actual = userController.createUser(request);
        assertEquals(expected, actual);
        verify(userService).createUser(request);
    }

    @Test
    void findUserById_ShouldDelegateToService() {
        UserResponse expected = new UserResponse(1L, "Jane", "jane@example.com", 25, LocalDateTime.now());
        when(userService.findUserById(1L)).thenReturn(expected);

        UserResponse actual = userController.findUserById(1L);
        assertEquals(expected, actual);
        verify(userService).findUserById(1L);
    }

    @Test
    void updateUser_ShouldDelegateToService() {
        UserRequest request = new UserRequest("New", "new@example.com", 26);
        UserResponse expected = new UserResponse(1L, "New", "new@example.com", 26, LocalDateTime.now());
        when(userService.updateUser(1L, request)).thenReturn(expected);

        UserResponse actual = userController.updateUser(1L, request);
        assertEquals(expected, actual);
        verify(userService).updateUser(1L, request);
    }

    @Test
    void deleteUser_ShouldDelegateToService() {
        doNothing().when(userService).deleteUser(1L);
        userController.deleteUser(1L);
        verify(userService).deleteUser(1L);
    }

    @Test
    void findUserByEmail_ShouldDelegateToService() {
        String email = "test@example.com";
        UserResponse expected = new UserResponse(1L, "John", email, 30, LocalDateTime.now());
        when(userService.findUserByEmail(email)).thenReturn(expected);

        UserResponse actual = userController.findUserByEmail(email);
        assertEquals(expected, actual);
        verify(userService).findUserByEmail(email);
    }
}
