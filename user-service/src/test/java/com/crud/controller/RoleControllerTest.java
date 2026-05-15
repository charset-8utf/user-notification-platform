package com.crud.controller;

import com.crud.config.SecurityConfig;
import tools.jackson.databind.json.JsonMapper;
import com.crud.dto.RoleRequest;
import com.crud.dto.RoleResponse;
import com.crud.exception.RoleNotFoundException;
import com.crud.service.RoleService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoleController.class)
@Import(SecurityConfig.class)
@WithMockUser
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoleService roleService;

    private final JsonMapper jsonMapper = JsonMapper.shared();

    @Test
    void createRole_ShouldReturn201() throws Exception {
        RoleRequest request = new RoleRequest("ADMIN");
        RoleResponse response = new RoleResponse(1L, "ADMIN", LocalDateTime.now(), LocalDateTime.now());

        when(roleService.createRole(any(RoleRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("ADMIN"));
    }

    @Test
    void findRoleById_ShouldReturn200() throws Exception {
        RoleResponse response = new RoleResponse(1L, "USER", LocalDateTime.now(), LocalDateTime.now());

        when(roleService.findRoleById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("USER"));
    }

    @Test
    void findRoleById_WhenNotFound_ShouldReturn404() throws Exception {
        when(roleService.findRoleById(999L)).thenThrow(new RoleNotFoundException(999L));

        mockMvc.perform(get("/api/roles/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void findAllRoles_ShouldReturn200() throws Exception {
        RoleResponse response = new RoleResponse(1L, "ADMIN", LocalDateTime.now(), LocalDateTime.now());

        when(roleService.findAllRoles(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/roles")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void updateRole_ShouldReturn200() throws Exception {
        RoleRequest request = new RoleRequest("SUPER_ADMIN");
        RoleResponse response = new RoleResponse(1L, "SUPER_ADMIN", LocalDateTime.now(), LocalDateTime.now());

        when(roleService.updateRole(anyLong(), any(RoleRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("SUPER_ADMIN"));
    }

    @Test
    void deleteRole_ShouldReturn204() throws Exception {
        doNothing().when(roleService).deleteRole(1L);

        mockMvc.perform(delete("/api/roles/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignRoleToUser_ShouldReturn200() throws Exception {
        doNothing().when(roleService).assignRoleToUser(1L, 2L);

        mockMvc.perform(post("/api/roles/assign")
                        .param("userId", "1")
                        .param("roleId", "2"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void removeRoleFromUser_ShouldReturn200() throws Exception {
        doNothing().when(roleService).removeRoleFromUser(1L, 2L);

        mockMvc.perform(post("/api/roles/remove")
                        .param("userId", "1")
                        .param("roleId", "2"))
                .andExpect(status().isOk());
    }
}
