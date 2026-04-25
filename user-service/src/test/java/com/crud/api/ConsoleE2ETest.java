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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.Timeout;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(value = 60, unit = TimeUnit.SECONDS)
@Testcontainers
class ConsoleE2ETest {
    @Container
    private static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine")
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
        String createInput = "1\nИван\nivan@example.com\n30\n0\n";
        simulateUserInput(createInput);
        Console consoleCreate = new Console(userController);
        consoleCreate.start();
        String createOutput = outContent.toString();
        outContent.reset();

        String marker = "✅ Пользователь создан! ID: ";
        int startIdx = createOutput.indexOf(marker);
        assertNotEquals(-1, startIdx, "Не найдено сообщение о создании пользователя");

        int endIdx = createOutput.indexOf('\n', startIdx);
        if (endIdx == -1) {
            endIdx = createOutput.length();
        }
        String idStr = createOutput.substring(startIdx + marker.length(), endIdx).trim();
        long userId = Long.parseLong(idStr);

        String findInput = "2\n" + userId + "\n0\n";
        simulateUserInput(findInput);
        Console consoleFind = new Console(userController);
        consoleFind.start();
        String findOutput = outContent.toString();

        assertTrue(findOutput.contains("🔍 Найден пользователь:"),
                "Должно быть сообщение о найденном пользователе");
        assertTrue(findOutput.contains("Имя: Иван"),
                "Должно содержать имя Иван");
        assertTrue(findOutput.contains("Email: ivan@example.com"),
                "Должен содержать email ivan@example.com");
    }


    @Test
    void testFindUserByEmail_Success() {
        UserResponse created = userController.createUser(
                new UserRequest("EmailTest", "email@example.com", 30)
        );
        String email = created.email();

        String simulatedInput = "3\n" + email + "\n0\n";
        simulateUserInput(simulatedInput);

        Console console = new Console(userController);
        console.start();

        String output = outContent.toString();
        assertTrue(output.contains("🔍 Найден пользователь:"));
        assertTrue(output.contains("Email: " + email));
        assertTrue(output.contains("Имя: EmailTest"));
    }

    @Test
    void testFindUserByEmail_NotFound() {
        String simulatedInput = "3\nnonexistent@example.com\n0\n";
        simulateUserInput(simulatedInput);

        Console console = new Console(userController);
        console.start();

        String output = outContent.toString();
        assertTrue(output.contains("❌ Ошибка: Пользователь с email nonexistent@example.com не найден"));
    }

    @Test
    void testListUsers() {
        userController.createUser(new UserRequest("Alice", "alice@example.com", 25));
        userController.createUser(new UserRequest("Bob", "bob@example.com", 30));

        String simulatedInput = "6\n0\n";
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
        String simulatedInput = "5\n" + created.id() + "\ny\n0\n";
        simulateUserInput(simulatedInput);

        Console console = new Console(userController);
        console.start();

        String output = outContent.toString();
        assertTrue(output.contains("✅ Пользователь с ID " + created.id() + " удалён"));
    }

    @ParameterizedTest
    @CsvSource({
            "Новое имя, ,30",
            " ,new@example.com,30",
            " , ,35",
            "Новое имя,new@example.com,35",
            "Новое имя, ,",
            " ,new@example.com,",
    })

    void testUpdateUser_PartialUpdates(String newName, String newEmail, String newAgeStr) {
        UserResponse original = userController.createUser(
                new UserRequest("OriginalName", "original@example.com", 30)
        );

        ExpectedValues expected = ExpectedValues.builder()
                .name(defaultIfBlank(newName, original.name()))
                .email(defaultIfBlank(newEmail, original.email()))
                .age(defaultIfBlankInt(newAgeStr, original.age()))
                .build();

        String userInput = buildUpdateInput(original.id(), newName, newEmail, newAgeStr);
        simulateUserInput(userInput);

        new Console(userController).start();

        assertTrue(outContent.toString().contains("✅ Пользователь с ID " + original.id() + " обновлён"));
        UserResponse updated = userController.findUserById(original.id());
        assertAll("Обновлённый пользователь",
                () -> assertEquals(expected.name(), updated.name(), "Имя"),
                () -> assertEquals(expected.email(), updated.email(), "Email"),
                () -> assertEquals(expected.age(), updated.age(), "Возраст")
        );
    }

    private String buildUpdateInput(long id, String newName, String newEmail, String newAgeStr) {
        return new InputBuilder()
                .addLine("4")
                .addLine(String.valueOf(id))
                .addLineOrDefault(newName)
                .addLineOrDefault(newEmail)
                .addLineOrDefault(newAgeStr)
                .addLine("0")
                .build();
    }

    private static String defaultIfBlank(String value, String defaultValue) {
        return (value == null || value.isBlank()) ? defaultValue : value.trim();
    }

    private static int defaultIfBlankInt(String value, int defaultValue) {
        return (value == null || value.isBlank()) ? defaultValue : Integer.parseInt(value.trim());
    }

    private static class InputBuilder {

        private final StringBuilder sb = new StringBuilder();

        InputBuilder addLine(String line) {
            sb.append(line).append("\n");
            return this;
        }

        InputBuilder addLineOrDefault(String value) {
            if (value == null || value.isBlank()) {
                sb.append("\n");
            } else {
                sb.append(value).append("\n");
            }
            return this;
        }

        String build() {
            return sb.toString();
        }
    }

    private record ExpectedValues(String name, String email, int age) {

        static Builder builder() {
                return new Builder();
        }

        static class Builder {
                private String name;
                private String email;
                private int age;

            Builder name(String name) {
                this.name = name;
                return this;
            }

            Builder email(String email) {
                this.email = email;
                return this;
            }

            Builder age(int age) {
                this.age = age;
                return this;
            }

            ExpectedValues build() {
                return new ExpectedValues(name, email, age);
            }
        }
    }
}