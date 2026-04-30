package com.crud.repository;

import com.crud.entity.Note;
import com.crud.entity.Profile;
import com.crud.entity.Role;
import com.crud.entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.jetbrains.annotations.NotNull;
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
class NoteRepositoryImplIntegrationTest {
    @Container
    private static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private static SessionFactory sessionFactory;
    private NoteRepository noteRepository;
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
        noteRepository = new NoteRepositoryImpl(sessionFactory);
        userRepository = new UserRepositoryImpl(sessionFactory);
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.createMutationQuery("DELETE FROM Note").executeUpdate();
            session.createMutationQuery("DELETE FROM User").executeUpdate();
            tx.commit();
        }
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

    @Test
    void save_ShouldPersistNote() {
        User user = User.builder().name("NoteUser").email("note@example.com").age(25).build();
        User savedUser = userRepository.save(user);

        Note note = Note.builder().content("Test note content").user(savedUser).build();
        Note saved = noteRepository.save(note);

        assertNotNull(saved.getId());
        assertEquals("Test note content", saved.getContent());
    }

    @Test
    void findById_WhenExists_ShouldReturnNote() {
        User user = User.builder().name("NoteUser2").email("note2@example.com").age(25).build();
        User savedUser = userRepository.save(user);
        Note note = Note.builder().content("Find me").user(savedUser).build();
        Note saved = noteRepository.save(note);

        Optional<Note> found = noteRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Find me", found.get().getContent());
    }

    @Test
    void secondLevelCache_ShouldReturnSameNote() {
        User user = User.builder().name("CacheNoteUser").email("cachenote@example.com").age(25).build();
        User savedUser = userRepository.save(user);
        Note note = Note.builder().content("Cached note").user(savedUser).build();
        noteRepository.save(note);
        Long id = note.getId();

        Optional<Note> found1 = noteRepository.findById(id);
        Optional<Note> found2 = noteRepository.findById(id);

        assertTrue(found1.isPresent());
        assertTrue(found2.isPresent());
        assertEquals(found1.get().getId(), found2.get().getId());
    }

    @Test
    void deleteById_ShouldRemoveNote() {
        User user = User.builder().name("DeleteNoteUser").email("deletenote@example.com").age(25).build();
        User savedUser = userRepository.save(user);
        Note note = Note.builder().content("To delete").user(savedUser).build();
        Note saved = noteRepository.save(note);

        noteRepository.deleteById(saved.getId());

        Optional<Note> found = noteRepository.findById(saved.getId());
        assertTrue(found.isEmpty());
    }
}
