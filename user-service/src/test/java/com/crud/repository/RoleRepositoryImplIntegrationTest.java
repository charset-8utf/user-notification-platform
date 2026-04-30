package com.crud.repository;

import com.crud.dto.Page;
import com.crud.dto.Pageable;
import com.crud.entity.Note;
import com.crud.entity.Profile;
import com.crud.entity.Role;
import com.crud.entity.User;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Assumptions;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.flywaydb.core.Flyway;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers(disabledWithoutDocker = true)
class RoleRepositoryImplIntegrationTest {
    @Container
    private static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private static SessionFactory sessionFactory;
    private RoleRepository roleRepository;
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

    @BeforeEach
    void setUp() {
        roleRepository = new RoleRepositoryImpl(sessionFactory);
        userRepository = new UserRepositoryImpl(sessionFactory);
    }

    @AfterAll
    static void afterAll() {
        if (sessionFactory != null)
            sessionFactory.close();
        postgres.stop();
    }

    private static Configuration getConfiguration() {
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

    @Test
    void save_ShouldPersistRole() {
        long uniqueId1 = System.currentTimeMillis();
        Role role = Role.builder().id(uniqueId1).name("TEST_ADMIN").build();
        Role saved = roleRepository.save(role);

        assertNotNull(saved.getId());
        assertEquals("TEST_ADMIN", saved.getName());
    }

    @Test
    void findById_WhenExists_ShouldReturnRole() {
        long uniqueId2 = System.currentTimeMillis();
        Role role = Role.builder().id(uniqueId2).name("TEST_USER").build();
        Role saved = roleRepository.save(role);

        Optional<Role> found = roleRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("TEST_USER", found.get().getName());
    }

    @Test
    void secondLevelCache_ShouldReturnSameRole() {
        long uniqueId3 = System.currentTimeMillis() + 1;
        Role role = Role.builder().id(uniqueId3).name("CacheTest").build();
        roleRepository.save(role);
        Long id = role.getId();

        Optional<Role> found1 = roleRepository.findById(id);
        Optional<Role> found2 = roleRepository.findById(id);

        assertTrue(found1.isPresent());
        assertTrue(found2.isPresent());
        assertEquals(found1.get().getId(), found2.get().getId());
    }

    @Test
    void findAllRoles_ShouldReturnAllRoles() {
        long uniqueId4 = System.currentTimeMillis();
        long uniqueId5 = uniqueId4 + 1;
        roleRepository.save(Role.builder().id(uniqueId4).name("Role1").build());
        roleRepository.save(Role.builder().id(uniqueId5).name("Role2").build());

        Page<Role> page = roleRepository.findAll(Pageable.of(0, 10));

        assertTrue(page.content().size() >= 2);
    }

    @Test
    void assignRoleToUser_ShouldNotThrow() {
        User user = User.builder().name("TestUser").email("testuser@example.com").age(25).build();
        User savedUser = userRepository.save(user);
        long uniqueId6 = System.currentTimeMillis();
        Role role = Role.builder().id(uniqueId6).name("ASSIGN_TEST").build();
        Role savedRole = roleRepository.save(role);

        assertDoesNotThrow(() -> roleRepository.assignRoleToUser(savedUser.getId(), savedRole.getId()));
    }

    @Test
    void removeRoleFromUser_ShouldNotThrow() {
        User user = User.builder().name("TestUser2").email("testuser2@example.com").age(25).build();
        User savedUser = userRepository.save(user);
        long uniqueId7 = System.currentTimeMillis();
        Role role = Role.builder().id(uniqueId7).name("REMOVE_TEST").build();
        Role savedRole = roleRepository.save(role);
        roleRepository.assignRoleToUser(savedUser.getId(), savedRole.getId());

        assertDoesNotThrow(() -> roleRepository.removeRoleFromUser(savedUser.getId(), savedRole.getId()));
    }

}
