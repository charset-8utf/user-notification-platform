package com.crud.repository;

import com.crud.entity.User;
import com.crud.exception.DataAccessException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Timeout;
import java.util.concurrent.TimeUnit;

@Timeout(value = 30, unit = TimeUnit.SECONDS)
@Testcontainers
class UserRepositoryImplIntegrationTest {
    @Container
    private static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private static SessionFactory sessionFactory;
    private UserRepository userRepository;

    @BeforeAll
    static void beforeAll() {
        postgres.start();
        Configuration cfg = new Configuration();
        cfg.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        cfg.setProperty("hibernate.connection.url", postgres.getJdbcUrl());
        cfg.setProperty("hibernate.connection.username", postgres.getUsername());
        cfg.setProperty("hibernate.connection.password", postgres.getPassword());
        cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        cfg.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        cfg.setProperty("hibernate.show_sql", "true");
        cfg.setProperty("hibernate.format_sql", "true");
        cfg.addAnnotatedClass(User.class);
        sessionFactory = cfg.buildSessionFactory();
    }

    @AfterAll
    static void afterAll() {
        if (sessionFactory != null) sessionFactory.close();
        postgres.stop();
    }

    @BeforeEach
    void setUp() {
        userRepository = new UserRepositoryImpl(sessionFactory);
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.createMutationQuery("DELETE FROM User").executeUpdate();
            tx.commit();
        }
    }

    @Test
    void save_ShouldPersistUserAndGenerateId() {
        User user = User.builder().name("Alice").email("alice@example.com").age(25).build();
        User saved = userRepository.save(user);
        assertNotNull(saved.getId());
        assertEquals("Alice", saved.getName());
        assertEquals("alice@example.com", saved.getEmail());
        assertEquals(25, saved.getAge());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void save_WhenDuplicateEmail_ShouldThrowDataAccessException() {
        User user1 = User.builder().name("Bob").email("bob@example.com").age(30).build();
        userRepository.save(user1);
        User user2 = User.builder().name("Bob2").email("bob@example.com").age(31).build();
        assertThrows(DataAccessException.class, () -> userRepository.save(user2));
    }

    @Test
    void findById_WhenExists_ShouldReturnUser() {
        User user = User.builder().name("Charlie").email("charlie@example.com").age(28).build();
        User saved = userRepository.save(user);
        Optional<User> found = userRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Charlie", found.get().getName());
    }

    @Test
    void findById_WhenNotExists_ShouldReturnEmpty() {
        Optional<User> found = userRepository.findById(999L);
        assertTrue(found.isEmpty());
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        userRepository.save(User.builder().name("User1").email("u1@example.com").age(20).build());
        userRepository.save(User.builder().name("User2").email("u2@example.com").age(21).build());
        List<User> users = userRepository.findAll();
        assertEquals(2, users.size());
    }

    @Test
    void update_ShouldModifyUser() {
        User user = User.builder().name("Before").email("before@example.com").age(25).build();
        User saved = userRepository.save(user);
        saved.setName("After");
        saved.setEmail("after@example.com");
        saved.setAge(26);
        User updated = userRepository.update(saved);
        assertEquals("After", updated.getName());
        assertEquals("after@example.com", updated.getEmail());
        assertEquals(26, updated.getAge());
    }

    @Test
    void deleteById_ShouldRemoveUser() {
        User user = User.builder().name("ToDelete").email("delete@example.com").age(40).build();
        User saved = userRepository.save(user);
        userRepository.deleteById(saved.getId());
        Optional<User> found = userRepository.findById(saved.getId());
        assertTrue(found.isEmpty());
    }

    @Test
    void update_WhenUserNotFound_ShouldThrowDataAccessException() {
        User nonExistent = User.builder().name("Ghost").email("ghost@example.com").age(0).build();
        nonExistent.setId(999L);
        assertThrows(DataAccessException.class, () -> userRepository.update(nonExistent));
    }

    @Test
    void deleteById_WhenNotFound_ShouldNotThrow() {
        long id = 999L;
        assertDoesNotThrow(() -> userRepository.deleteById(id));
    }

    @Test
    void findByEmail_WhenExists_ShouldReturnUser() {
        String email = "unique@example.com";
        User user = User.builder().name("EmailTest").email(email).age(25).build();
        userRepository.save(user);
        Optional<User> found = userRepository.findByEmail(email);
        assertTrue(found.isPresent());
        assertEquals(email, found.get().getEmail());
    }

    @Test
    void findByEmail_WhenNotExists_ShouldReturnEmpty() {
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");
        assertTrue(found.isEmpty());
    }

    @Test
    void update_WhenConcurrentModification_ShouldThrowOptimisticLockException() {
        User user = User.builder().name("Исходный").email("optimistic@example.com").age(20).build();
        User saved = userRepository.save(user);
        Long id = saved.getId();

        User userV1 = userRepository.findById(id).orElseThrow();
        User userV2 = userRepository.findById(id).orElseThrow();

        userV1.setName("Обновлён первым");
        userV2.setName("Обновлён вторым");

        userRepository.update(userV1);

        assertThrows(DataAccessException.class, () -> userRepository.update(userV2));
    }
}