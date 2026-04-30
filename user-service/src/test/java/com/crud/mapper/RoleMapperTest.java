package com.crud.mapper;

import com.crud.dto.RoleRequest;
import com.crud.dto.RoleResponse;
import com.crud.entity.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoleMapperTest {

    private final RoleMapper mapper = new RoleMapperImpl();

    @Test
    void toEntity_ShouldMapRequestToRole() {
        RoleRequest request = new RoleRequest(1L, "ADMIN");

        Role result = mapper.toEntity(request);

        assertEquals(1L, result.getId());
        assertEquals("ADMIN", result.getName());
    }

    @Test
    void toResponse_ShouldMapRoleToResponse() {
        Role role = Role.builder().name("USER").build();
        role.setId(1L);
        role.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        role.setUpdatedAt(LocalDateTime.of(2025, 1, 2, 12, 0));

        RoleResponse result = mapper.toResponse(role);

        assertEquals(1L, result.id());
        assertEquals("USER", result.name());
        assertNotNull(result.createdAt());
        assertNotNull(result.updatedAt());
    }

    @Test
    void toResponseList_ShouldMapListOfRoles() {
        Role role1 = Role.builder().name("ADMIN").build();
        role1.setId(1L);
        role1.setCreatedAt(LocalDateTime.now());
        Role role2 = Role.builder().name("USER").build();
        role2.setId(2L);
        role2.setCreatedAt(LocalDateTime.now());

        List<RoleResponse> result = mapper.toResponseList(List.of(role1, role2));

        assertEquals(2, result.size());
        assertEquals("ADMIN", result.get(0).name());
        assertEquals("USER", result.get(1).name());
    }

    @Test
    void toResponseList_WithNull_ShouldReturnEmptyList() {
        List<RoleResponse> result = mapper.toResponseList(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void toEntity_WithExisting_ShouldUpdateRole() {
        Role existing = Role.builder().name("OldName").build();
        existing.setId(1L);

        RoleRequest request = new RoleRequest(null, "NewName");
        Role result = mapper.toEntity(request, existing);

        assertEquals("NewName", result.getName());
        assertSame(existing, result);
    }
}
