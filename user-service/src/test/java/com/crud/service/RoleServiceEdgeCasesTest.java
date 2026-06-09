package com.crud.service;

import com.crud.dto.RoleRequest;
import com.crud.dto.RoleResponse;
import com.crud.entity.Role;
import com.crud.entity.User;
import com.crud.exception.RoleNotFoundException;
import com.crud.exception.UserNotFoundException;
import com.crud.exception.UserServiceException;
import com.crud.mapper.RoleMapper;
import com.crud.repository.RoleRepository;
import com.crud.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
class RoleServiceEdgeCasesTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Test
    void createRole_WithDuplicateName_ShouldThrowException() {
        RoleRequest request = new RoleRequest("ADMIN");
        Role role = roleNamed("ADMIN");
        when(roleMapper.toEntity(request)).thenReturn(role);
        when(roleRepository.save(any())).thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThatThrownBy(() -> roleService.createRole(request))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("ADMIN");
    }

    @Test
    void findRoleById_WhenNotFound_ShouldThrowException() {
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.findRoleById(999L))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void updateRole_WhenNotFound_ShouldThrowException() {
        RoleRequest request = new RoleRequest("NEW_ROLE");
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.updateRole(999L, request))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void deleteRole_WhenNotFound_ShouldThrowException() {
        when(roleRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> roleService.deleteRole(999L))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void assignRoleToUser_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.assignRoleToUser(999L, 1L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void assignRoleToUser_WhenRoleNotFound_ShouldThrowException() {
        User user = User.builder().name("John").email("john@test.com").age(30).build();
        user.setId(1L);
        user.setRoles(new HashSet<>());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.assignRoleToUser(1L, 999L))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void removeRoleFromUser_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.removeRoleFromUser(999L, 1L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void removeRoleFromUser_WhenRoleNotFound_ShouldThrowException() {
        User user = User.builder().name("John").email("john@test.com").age(30).build();
        user.setId(1L);
        user.setRoles(new HashSet<>());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.removeRoleFromUser(1L, 999L))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessageContaining("999");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "USER", "MODERATOR", "SUPER_ADMIN"})
    void createRole_WithValidNames_ShouldWork(String roleName) {
        RoleRequest request = new RoleRequest(roleName);
        Role role = roleNamed(roleName);
        role.setId(1L);
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());

        when(roleMapper.toEntity(request)).thenReturn(role);
        when(roleRepository.save(any())).thenReturn(role);
        when(roleMapper.toResponse(any())).thenReturn(
                new RoleResponse(1L, roleName, LocalDateTime.now(), LocalDateTime.now()));

        RoleResponse result = roleService.createRole(request);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(roleName);
    }

    @ParameterizedTest
    @CsvSource({
            "1, true",
            "2, true",
            "999, false"
    })
    void deleteRole_WhenExistsCheck_ShouldVerifyBeforeDelete(Long id, boolean exists) {
        when(roleRepository.existsById(id)).thenReturn(exists);

        if (!exists) {
            assertThatThrownBy(() -> roleService.deleteRole(id))
                    .isInstanceOf(RoleNotFoundException.class);
        } else {
            roleService.deleteRole(id);
            verify(roleRepository).deleteById(id);
        }

        verify(roleRepository).existsById(id);
    }

    private static Role roleNamed(String name) {
        Role role = Role.builder().build();
        role.setName(name);
        return role;
    }
}
