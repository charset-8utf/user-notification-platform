package com.crud.service;

import com.crud.dto.RoleRequest;
import com.crud.dto.RoleResponse;
import com.crud.entity.Role;
import com.crud.entity.User;
import com.crud.exception.DataAccessException;
import com.crud.exception.RoleNotFoundException;
import com.crud.exception.UserNotFoundException;
import com.crud.exception.ValidationException;
import com.crud.repository.RoleRepository;
import com.crud.repository.UserRepository;
import org.hibernate.StaleObjectStateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void createRole_ShouldCreateAndReturnResponse() {
        RoleServiceImpl service = new RoleServiceImpl(roleRepository, userRepository);
        Role role = Role.builder().name("ADMIN").build();
        role.setId(1L);
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        RoleResponse result = service.createRole(new RoleRequest(1L, "ADMIN"));

        assertEquals(1L, result.id());
        assertEquals("ADMIN", result.name());
    }

    @Test
    void createRole_WhenNameBlank_ShouldThrowValidation() {
        RoleServiceImpl service = new RoleServiceImpl(roleRepository, userRepository);

        RoleRequest blankRequest = new RoleRequest(null, "");
        RoleRequest spaceRequest = new RoleRequest(null, "   ");
        RoleRequest nullRequest = new RoleRequest(null, null);
        assertThrows(ValidationException.class, () -> service.createRole(blankRequest));
        assertThrows(ValidationException.class, () -> service.createRole(spaceRequest));
        assertThrows(ValidationException.class, () -> service.createRole(nullRequest));
    }

    @Test
    void findRoleById_WhenExists_ShouldReturnResponse() {
        RoleServiceImpl service = new RoleServiceImpl(roleRepository, userRepository);
        Role role = Role.builder().name("USER").build();
        role.setId(1L);
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        RoleResponse result = service.findRoleById(1L);

        assertEquals(1L, result.id());
        assertEquals("USER", result.name());
    }

    @Test
    void findRoleById_WhenNotFound_ShouldThrow() {
        RoleServiceImpl service = new RoleServiceImpl(roleRepository, userRepository);
        when(roleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, () -> service.findRoleById(1L));
    }

    @Test
    void updateRole_ShouldUpdateAndReturnResponse() {
        RoleServiceImpl service = new RoleServiceImpl(roleRepository, userRepository);
        Role role = Role.builder().name("OldName").build();
        role.setId(1L);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleRepository.update(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));

        RoleResponse result = service.updateRole(1L, new RoleRequest(null, "NewName"));

        assertEquals(1L, result.id());
        assertEquals("NewName", result.name());
    }

    @Test
    void deleteRole_ShouldCallRepositoryDelete() {
        RoleServiceImpl service = new RoleServiceImpl(roleRepository, userRepository);
        doNothing().when(roleRepository).deleteById(1L);

        service.deleteRole(1L);

        verify(roleRepository).deleteById(1L);
    }

    @Test
    void assignRoleToUser_ShouldCallRepository() {
        RoleServiceImpl service = new RoleServiceImpl(roleRepository, userRepository);
        User user = User.builder().name("John").email("john@example.com").age(25).build();
        user.setId(1L);
        Role role = Role.builder().name("ADMIN").build();
        role.setId(2L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(role));
        doNothing().when(roleRepository).assignRoleToUser(1L, 2L);

        service.assignRoleToUser(1L, 2L);

        verify(roleRepository).assignRoleToUser(1L, 2L);
    }

    @Test
    void assignRoleToUser_WhenUserNotFound_ShouldThrow() {
        RoleServiceImpl service = new RoleServiceImpl(roleRepository, userRepository);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.assignRoleToUser(1L, 2L));
    }

    @Test
    void removeRoleFromUser_ShouldCallRepository() {
        RoleServiceImpl service = new RoleServiceImpl(roleRepository, userRepository);
        doNothing().when(roleRepository).removeRoleFromUser(1L, 2L);

        service.removeRoleFromUser(1L, 2L);

        verify(roleRepository).removeRoleFromUser(1L, 2L);
    }

    @Test
    void updateRole_WithOptimisticLockException_ShouldRetryAndSucceed() {
        RoleServiceImpl service = new RoleServiceImpl(roleRepository, userRepository);
        Role role = Role.builder().name("Old").build();
        role.setId(1L);
        when(roleRepository.findById(1L))
                .thenReturn(Optional.of(role))
                .thenReturn(Optional.of(role))
                .thenReturn(Optional.of(role));
        when(roleRepository.update(any(Role.class)))
                .thenThrow(new DataAccessException("Optimistic lock", new StaleObjectStateException("Role", 1L)))
                .thenThrow(new DataAccessException("Optimistic lock", new StaleObjectStateException("Role", 1L)))
                .thenAnswer(inv -> inv.getArgument(0));

        RoleResponse result = service.updateRole(1L, new RoleRequest("New"));

        assertNotNull(result);
        verify(roleRepository, times(3)).update(any(Role.class));
    }
}
