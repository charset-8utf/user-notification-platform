package com.crud.repository;

import com.crud.entity.Note;
import com.crud.entity.Profile;
import com.crud.entity.Role;
import com.crud.entity.User;
import com.crud.exception.DataAccessException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Assumptions;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.flywaydb.core.Flyway;
import org.hibernate.stat.Statistics;


import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Timeout;
import java.util.concurrent.TimeUnit;

@Timeout(value = 30, unit = TimeUnit.SECONDS)
@Testcontainers(disabledWithoutDocker = true)
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
        Assumptions.assumeTrue(
                DockerClientFactory.instance().isDockerAvailable(),
                "Docker недоступен: интеграционные тесты пропущены"
        );
        postgres.start();
        Flyway flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("db/migration")
                .load();
        flyway.migrate();

        Configuration cfg = getConfiguration();
        cfg.addAnnotatedClass(User.class);
        cfg.addAnnotatedClass(Note.class);
        cfg.addAnnotatedClass(Role.class);
        cfg.addAnnotatedClass(Profile.class);
        sessionFactory = cfg.buildSessionFactory();
    }

    private static @NotNull Configuration getConfiguration() {
        Configuration cfg = new Configuration();
        cfg.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        cfg.setProperty("hibernate.connection.url", postgres.getJdbcUrl());
        cfg.setProperty("hibernate.connection.username", postgres.getUsername());
        cfg.setProperty("hibernate.connection.password", postgres.getPassword());
        cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        cfg.setProperty("hibernate.hbm2ddl.auto", "validate");
        cfg.setProperty("hibernate.show_sql", "true");
        cfg.setProperty("hibernate.format_sql", "true");
        cfg.setProperty("hibernate.jdbc.batch_size", "20");
        cfg.setProperty("hibernate.order_inserts", "true");
        cfg.setProperty("hibernate.order_updates", "true");
        cfg.setProperty("hibernate.cache.use_second_level_cache", "true");
        cfg.setProperty("hibernate.cache.region.factory_class", "org.hibernate.cache.jcache.JCacheRegionFactory");
        cfg.setProperty("hibernate.javax.cache.provider", "com.github.benmanes.caffeine.jcache.spi.CaffeineCachingProvider");
        cfg.setProperty("hibernate.cache.use_query_cache", "true");
        cfg.setProperty("hibernate.cache.caffeine.maxSize", "10000");
        return cfg;
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
        com.crud.dto.Page<User> page = userRepository.findAll(com.crud.dto.Pageable.of(0, 10));
        assertEquals(2, page.content().size());
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
    void batchInsert_ShouldSaveAllUsersWithReducedQueries() {
        Statistics stats = sessionFactory.unwrap(SessionFactory.class).getStatistics();
        stats.clear();

        int numberOfUsers = 100;
        for (int i = 0; i < numberOfUsers; i++) {
            User user = User.builder()
                    .name("BatchUser" + i)
                    .email("batch" + i + "@example.com")
                    .age(20 + i % 50)
                    .build();
            userRepository.save(user);
        }

        long queryCount = stats.getQueryExecutionCount();
        int expectedMaxQueries = (numberOfUsers / 20) + 5;
        assertTrue(queryCount <= expectedMaxQueries,
                "Ожидалось не более " + expectedMaxQueries + " запросов, а было " + queryCount);
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

    @Test
    void findByIdWithLock_WhenNotExists_ShouldReturnEmpty() {
        Optional<User> found = userRepository.findByIdWithLock(9999L);
        assertTrue(found.isEmpty());
    }

    @Test
    void secondLevelCache_ShouldReturnSameUser() {
        User user = User.builder().name("CacheTest").email("cache@test.com").age(25).build();
        userRepository.save(user);
        Long id = user.getId();

        Optional<User> found1 = userRepository.findById(id);
        Optional<User> found2 = userRepository.findById(id);

        assertTrue(found1.isPresent());
        assertTrue(found2.isPresent());
        assertEquals(found1.get().getId(), found2.get().getId());
    }
}
