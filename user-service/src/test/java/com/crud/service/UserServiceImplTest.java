package com.crud.service;

import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import com.crud.entity.User;
import com.crud.exception.DataAccessException;
import com.crud.exception.UserNotFoundException;
import com.crud.exception.ValidationException;
import com.crud.mapper.UserMapper;
import com.crud.mapper.UserMapperImpl;
import com.crud.repository.UserRepository;
import org.hibernate.StaleObjectStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private UserServiceImpl userService;
    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapperImpl();
        userService = new UserServiceImpl(userRepository, userMapper);
    }

    @Test
    void createUser_ShouldSaveAndReturnResponse() {
        UserRequest request = new UserRequest("John", "john@example.com", 30);
        User savedUser = userMapper.toEntity(request);
        savedUser.setId(1L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.createUser(request);
        assertNotNull(response.id());
        assertEquals("John", response.name());
        assertEquals("john@example.com", response.email());
        assertEquals(30, response.age());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_WithInvalidName_ShouldThrowValidationException() {
        UserRequest request = new UserRequest("", "john@example.com", 30);
        assertThrows(ValidationException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "not-an-email", "user@", "@example.com", "user@.com", "user@domain..com"})
    void createUser_WithInvalidEmail_ShouldThrowValidationException(String invalidEmail) {
        UserRequest request = new UserRequest("John", invalidEmail, 30);
        assertThrows(ValidationException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any());
    }

    @ParameterizedTest
    @CsvSource({"-1", "151", "-5"})
    void createUser_WithInvalidAge_ShouldThrowValidationException(int age) {
        UserRequest request = new UserRequest("John", "john@example.com", age);
        assertThrows(ValidationException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void findUserById_WhenExists_ShouldReturnResponse() {
        User user = User.builder().name("Jane").email("jane@example.com").age(25).build();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.findUserById(1L);
        assertEquals(1L, response.id());
        assertEquals("Jane", response.name());
    }

    @Test
    void findUserById_WhenNotExists_ShouldThrowUserNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.findUserById(999L));
    }

    @Test
    void updateUser_ShouldUpdateAndReturnResponse() {
        User existing = User.builder().name("Old").email("old@example.com").age(20).build();
        existing.setId(1L);
        UserRequest request = new UserRequest("New", "new@example.com", 25);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.update(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.updateUser(1L, request);
        assertEquals("New", response.name());
        assertEquals("new@example.com", response.email());
        assertEquals(25, response.age());
    }

    @Test
    void deleteUser_ShouldCallRepositoryDelete() {
        User user = User.builder().name("ToDelete").email("delete@example.com").age(40).build();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_WhenNotExists_ShouldThrowUserNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(999L));
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void updateUser_WhenNotExists_ShouldThrowUserNotFoundException() {
        UserRequest request = new UserRequest("New", "new@example.com", 25);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.updateUser(999L, request));
        verify(userRepository, never()).update(any());
    }

    @Test
    void findUserByEmail_WhenExists_ShouldReturnResponse() {
        String email = "test@example.com";
        User user = User.builder().name("John").email(email).age(30).build();
        user.setId(1L);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserResponse response = userService.findUserByEmail(email);
        assertEquals(1L, response.id());
        assertEquals("John", response.name());
        assertEquals(email, response.email());
        assertEquals(30, response.age());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void findUserByEmail_WhenNotExists_ShouldThrowUserNotFoundException() {
        String email = "missing@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.findUserByEmail(email));
        verify(userRepository).findByEmail(email);
    }

    @Test
    void updateUser_WithOptimisticLockException_ShouldRetryAndSucceed() {
        User existing = User.builder().name("Old").email("old@example.com").age(20).build();
        existing.setId(1L);
        UserRequest request = new UserRequest("New", "new@example.com", 25);
        StaleObjectStateException optimisticLockException = new StaleObjectStateException("User", 1L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(existing))
                .thenReturn(Optional.of(existing))
                .thenReturn(Optional.of(existing));
        when(userRepository.update(any(User.class)))
                .thenThrow(new DataAccessException("Конкурентное изменение", optimisticLockException))
                .thenThrow(new DataAccessException("Конкурентное изменение", optimisticLockException))
                .thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.updateUser(1L, request);

        assertEquals("New", response.name());
        assertEquals("new@example.com", response.email());
        assertEquals(25, response.age());

        verify(userRepository, times(3)).findById(1L);

        verify(userRepository, times(3)).update(any(User.class));
    }

    @Test
    void updateUser_WithPersistentOptimisticLockException_ShouldFailAfterMaxRetries() {
        User existing = User.builder().name("Old").email("old@example.com").age(20).build();
        existing.setId(1L);
        UserRequest request = new UserRequest("New", "new@example.com", 25);
        StaleObjectStateException optimisticLockException = new StaleObjectStateException("User", 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.update(any(User.class)))
                .thenThrow(new DataAccessException("Конкурентное изменение", optimisticLockException));

        assertThrows(DataAccessException.class, () -> userService.updateUser(1L, request));

        verify(userRepository, times(3)).findById(1L);
        verify(userRepository, times(3)).update(any(User.class));
    }
}