package com.crud.controller;

import com.crud.entity.NotificationDeliveryStatus;

import com.crud.config.SecurityConfig;
import com.crud.config.WebMvcTestSecuritySupport;
import tools.jackson.databind.json.JsonMapper;
import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import com.crud.exception.UserNotFoundException;
import com.crud.exception.ValidationException;
import com.crud.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, WebMvcTestSecuritySupport.class})
@WithMockUser
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private final JsonMapper jsonMapper = JsonMapper.shared();

    @Test
    void createUser_ShouldReturn201() throws Exception {
        UserRequest request = new UserRequest("John", "john@test.com", 30);
        UserResponse response = new UserResponse(1L, "John", "john@test.com", 30, NotificationDeliveryStatus.PENDING, LocalDateTime.now());

        when(userService.createUser(any(UserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john@test.com"));
    }

    @Test
    void createUser_WhenEmailExists_ShouldReturn400() throws Exception {
        UserRequest request = new UserRequest("John", "duplicate@test.com", 30);

        when(userService.createUser(any(UserRequest.class)))
                .thenThrow(new ValidationException("Email уже используется: duplicate@test.com"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findUserById_ShouldReturn200() throws Exception {
        UserResponse response = new UserResponse(1L, "Jane", "jane@test.com", 25, NotificationDeliveryStatus.PENDING, LocalDateTime.now());

        when(userService.findUserById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane"));
    }

    @Test
    void findUserById_WhenNotFound_ShouldReturn404() throws Exception {
        when(userService.findUserById(999L)).thenThrow(new UserNotFoundException(999L));

        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void findAllUsers_ShouldReturn200() throws Exception {
        UserResponse response = new UserResponse(1L, "John", "john@test.com", 30, NotificationDeliveryStatus.PENDING, LocalDateTime.now());
        when(userService.findAllUsers(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void updateUser_ShouldReturn200() throws Exception {
        UserRequest request = new UserRequest("Updated", "updated@test.com", 35);
        UserResponse response = new UserResponse(1L, "Updated", "updated@test.com", 35, NotificationDeliveryStatus.PENDING, LocalDateTime.now());

        when(userService.updateUser(anyLong(), any(UserRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void deleteUser_ShouldReturn204() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void findUserByEmail_ShouldReturn200() throws Exception {
        UserResponse response = new UserResponse(1L, "John", "john@test.com", 30, NotificationDeliveryStatus.PENDING, LocalDateTime.now());

        when(userService.findUserByEmail("john@test.com")).thenReturn(response);

        mockMvc.perform(get("/api/users/by-email")
                        .param("email", "john@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@test.com"));
    }

    @Test
    void searchUsers_ShouldReturn200() throws Exception {
        UserResponse response = new UserResponse(1L, "John", "john@gmail.com", 30, NotificationDeliveryStatus.PENDING, LocalDateTime.now());

        when(userService.searchUsersByEmail(eq("gmail"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/users/search")
                        .param("email", "gmail"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("john@gmail.com"));
    }
}
