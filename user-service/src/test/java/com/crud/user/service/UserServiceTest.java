package com.crud.user.service;

import com.crud.user.dao.UserDao;
import com.crud.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_ShouldDelegateToDaoAndValidate() {
        User userToCreate = new User("John", "john@example.com", 30);
        User savedUser = new User("John", "john@example.com", 30);
        savedUser.setId(1L);
        when(userDao.create(any(User.class))).thenReturn(savedUser);

        User result = userService.createUser("John", "john@example.com", 30);
        assertNotNull(result.getId());
        verify(userDao).create(any(User.class));
    }

    @Test
    void createUser_WithInvalidEmail_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("John", "not-an-email", 30));
    }

    @Test
    void findUserById_WhenExists_ReturnsUser() {
        User user = new User("Jane", "jane@example.com", 25);
        user.setId(1L);
        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        Optional<User> found = userService.findUserById(1L);
        assertTrue(found.isPresent());
        assertEquals("Jane", found.get().getName());
    }

    @Test
    void updateUser_ShouldModifyAndValidate() {
        User existing = new User("Old", "old@example.com", 20);
        existing.setId(1L);
        when(userDao.findById(1L)).thenReturn(Optional.of(existing));
        when(userDao.update(any(User.class))).thenReturn(existing);

        User updated = userService.updateUser(1L, "New", "new@example.com", 25);
        assertEquals("New", updated.getName());
        assertEquals("new@example.com", updated.getEmail());
        assertEquals(25, updated.getAge());
    }

    @Test
    void deleteUser_ShouldDelegateToDao() {
        doNothing().when(userDao).delete(1L);
        userService.deleteUser(1L);
        verify(userDao).delete(1L);
    }
}