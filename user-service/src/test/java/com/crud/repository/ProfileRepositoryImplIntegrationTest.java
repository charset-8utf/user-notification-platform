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
class ProfileRepositoryImplIntegrationTest {
    @Container
    private static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private static SessionFactory sessionFactory;
    private ProfileRepository profileRepository;
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
        profileRepository = new ProfileRepositoryImpl(sessionFactory);
        userRepository = new UserRepositoryImpl(sessionFactory);
    }

    @AfterAll
    static void afterAll() {
        if (sessionFactory != null) sessionFactory.close();
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
    void save_ShouldPersistProfile() {
        User user = User.builder().name("ProfileUser").email("profile@example.com").age(30).build();
        User savedUser = userRepository.save(user);

        Profile profile = Profile.builder().phone("+1234567890").address("Moscow").user(savedUser).build();
        Profile saved = profileRepository.save(profile);

        assertNotNull(saved.getId());
        assertEquals("+1234567890", saved.getPhone());
        assertEquals("Moscow", saved.getAddress());
    }

    @Test
    void findById_WhenExists_ShouldReturnProfile() {
        User user = User.builder().name("ProfileUser2").email("profile2@example.com").age(30).build();
        User savedUser = userRepository.save(user);
        Profile profile = Profile.builder().phone("+999").address("SPB").user(savedUser).build();
        Profile saved = profileRepository.save(profile);

        Optional<Profile> found = profileRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("+999", found.get().getPhone());
    }

    @Test
    void findByUserId_WhenExists_ShouldReturnProfile() {
        User user = User.builder().name("ProfileUser3").email("profile3@example.com").age(30).build();
        User savedUser = userRepository.save(user);
        Profile profile = Profile.builder().phone("+777").address("NY").user(savedUser).build();
        profileRepository.save(profile);

        Optional<Profile> found = profileRepository.findByUserId(savedUser.getId());

        assertTrue(found.isPresent());
        assertEquals("+777", found.get().getPhone());
    }

    @Test
    void secondLevelCache_ShouldReturnSameProfile() {
        User user = User.builder().name("CacheUser").email("cache@example.com").age(25).build();
        User savedUser = userRepository.save(user);
        Profile profile = Profile.builder().phone("+111").address("CacheCity").user(savedUser).build();
        profileRepository.save(profile);
        Long id = profile.getId();

        Optional<Profile> found1 = profileRepository.findById(id);
        Optional<Profile> found2 = profileRepository.findById(id);

        assertTrue(found1.isPresent());
        assertTrue(found2.isPresent());
        assertEquals(found1.get().getId(), found2.get().getId());
    }

    @Test
    void deleteByUserId_ShouldRemoveProfile() {
        User user = User.builder().name("DeleteUser").email("delete@example.com").age(30).build();
        User savedUser = userRepository.save(user);
        Profile profile = Profile.builder().phone("+333").address("DelCity").user(savedUser).build();
        profileRepository.save(profile);

        profileRepository.deleteByUserId(savedUser.getId());

        Optional<Profile> found = profileRepository.findByUserId(savedUser.getId());
        assertTrue(found.isEmpty());
    }

    @Test
    void findAll_WithPageable_ShouldReturnPage() {
        User user1 = User.builder().name("PageUser1").email("page1@example.com").age(25).build();
        User user2 = User.builder().name("PageUser2").email("page2@example.com").age(26).build();
        User savedUser1 = userRepository.save(user1);
        User savedUser2 = userRepository.save(user2);

        Profile profile1 = Profile.builder().phone("+111").address("City1").user(savedUser1).build();
        Profile profile2 = Profile.builder().phone("+222").address("City2").user(savedUser2).build();
        profileRepository.save(profile1);
        profileRepository.save(profile2);

        Page<Profile> page = profileRepository.findAll(Pageable.of(0, 10));

        assertTrue(page.content().size() >= 2);
        assertEquals(0, page.page());
        assertEquals(10, page.size());
    }
}
