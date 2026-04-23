package com.crud.api;

import com.crud.controller.UserController;
import com.crud.controller.UserControllerImpl;
import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import com.crud.repository.UserRepository;
import com.crud.repository.UserRepositoryImpl;
import com.crud.service.UserService;
import com.crud.service.UserServiceImpl;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({"deprecation", "resource"})
class ConsoleE2ETest {

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private static SessionFactory sessionFactory;
    private static UserController userController;
    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;

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
        cfg.addAnnotatedClass(com.crud.entity.User.class);
        sessionFactory = cfg.buildSessionFactory();

        UserRepository userRepository = new UserRepositoryImpl(sessionFactory);
        UserService userService = new UserServiceImpl(userRepository);
        userController = new UserControllerImpl(userService);
    }

    @AfterAll
    static void afterAll() {
        if (sessionFactory != null) sessionFactory.close();
        postgres.stop();
    }

    @BeforeEach
    void setUp() {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.createMutationQuery("DELETE FROM User").executeUpdate();
            tx.commit();
        }
        outContent = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    private void simulateUserInput(String input) {
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
    }

    @Test
    void testCreateAndFindUser() {
        String simulatedInput = "1\nИван\nivan@example.com\n30\n2\n1\n0\n";
        simulateUserInput(simulatedInput);

        Console console = new Console(userController);
        console.start();

        String output = outContent.toString();
        assertTrue(output.contains("✅ Пользователь создан! ID: 1"));
        assertTrue(output.contains("🔍 Найден пользователь:"));
        assertTrue(output.contains("Имя: Иван"));
        assertTrue(output.contains("Email: ivan@example.com"));
    }

    @Test
    void testListUsers() {
        userController.createUser(new UserRequest("Alice", "alice@example.com", 25));
        userController.createUser(new UserRequest("Bob", "bob@example.com", 30));

        String simulatedInput = "5\n0\n";
        simulateUserInput(simulatedInput);

        Console console = new Console(userController);
        console.start();

        String output = outContent.toString();
        assertTrue(output.contains("👥 Список пользователей:"));
        assertTrue(output.contains("Alice"));
        assertTrue(output.contains("Bob"));
    }

    @Test
    void testDeleteUser() {
        UserResponse created = userController.createUser(new UserRequest("ToDelete", "delete@example.com", 40));
        String simulatedInput = "4\n" + created.id() + "\ny\n0\n";
        simulateUserInput(simulatedInput);

        Console console = new Console(userController);
        console.start();

        String output = outContent.toString();
        assertTrue(output.contains("✅ Пользователь с ID " + created.id() + " удалён"));
    }
}