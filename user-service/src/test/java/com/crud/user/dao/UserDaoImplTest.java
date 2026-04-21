package com.crud.user.dao;

import com.crud.user.entity.User;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class UserDaoImplTest {
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private static SessionFactory sessionFactory;
    private UserDao userDao;

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @BeforeEach
    void setUp() {
        userDao = new UserDaoImpl(sessionFactory);
    }

    @Test
    void create_ShouldPersistUser() {
        User user = new User("Alice", "alice@example.com", 28);
        User saved = userDao.create(user);
        assertNotNull(saved.getId());
        assertEquals("Alice", saved.getName());
    }

    @Test
    void findById_ShouldReturnUser() {
        User user = new User("Bob", "bob@example.com", 32);
        User saved = userDao.create(user);
        Optional<User> found = userDao.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Bob", found.get().getName());
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        userDao.create(new User("User1", "u1@example.com", 20));
        userDao.create(new User("User2", "u2@example.com", 21));
        List<User> users = userDao.findAll();
        assertEquals(2, users.size());
    }

    @Test
    void update_ShouldModifyUser() {
        User user = new User("Before", "before@example.com", 25);
        User saved = userDao.create(user);
        saved.setName("After");
        userDao.update(saved);
        Optional<User> updated = userDao.findById(saved.getId());
        assertEquals("After", updated.get().getName());
    }

    @Test
    void delete_ShouldRemoveUser() {
        User user = new User("ToDelete", "delete@example.com", 40);
        User saved = userDao.create(user);
        userDao.delete(saved.getId());
        Optional<User> found = userDao.findById(saved.getId());
        assertTrue(found.isEmpty());
    }

    @AfterAll
    static void afterAll() {
        if (sessionFactory != null) sessionFactory.close();
        postgres.stop();
    }
}