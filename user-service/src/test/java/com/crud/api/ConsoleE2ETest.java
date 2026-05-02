package com.crud.api;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.crud.controller.*;
import com.crud.dto.*;
import com.crud.repository.*;
import com.crud.service.*;
import org.flywaydb.core.Flyway;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(value = 60, unit = TimeUnit.SECONDS)
@Testcontainers(disabledWithoutDocker = true)
class ConsoleE2ETest {
    @Container
    private static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private static SessionFactory sessionFactory;
    private static UserController userController;
    private static NoteController noteController;
    private static RoleController roleController;
    private static ProfileController profileController;
    private ListAppender<ILoggingEvent> logAppender;

    private void simulateUserInput(String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes()));
    }

    private void runConsole(String input) {
        simulateUserInput(input);
        new Console(userController, noteController, roleController, profileController).start();
    }

    private void assertOutputContains(String expected) {
        String logs = logAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.joining("\n"));
        assertTrue(logs.contains(expected),
                "Ожидалось '" + expected + "', но не найдено в логах");
    }

    private static class InputBuilder {
        private final StringBuilder sb = new StringBuilder();

        InputBuilder add(String line) {
            sb.append(line).append("\n");
            return this;
        }

        String build() {
            return sb.toString();
        }
    }

    private InputBuilder input() {
        return new InputBuilder();
    }

    private long createTestUser() {
        UserResponse user = userController.createUser(new UserRequest("NoteTester", "notes@ex.com", 25));
        return user.id();
    }

    @BeforeAll
    static void beforeAll() {
        Assumptions.assumeTrue(
                DockerClientFactory.instance().isDockerAvailable(),
                "Docker недоступен: сквозные тесты пропущены"
        );
        postgres.start();

        Flyway flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("db/migration")
                .load();
        flyway.migrate();

        System.setProperty("DB_URL", postgres.getJdbcUrl());
        System.setProperty("DB_USER", postgres.getUsername());
        System.setProperty("DB_PASSWORD", postgres.getPassword());

        com.crud.HibernateUtil hibernateUtil = new com.crud.HibernateUtil();
        sessionFactory = hibernateUtil.getSessionFactory();

        UserRepository userRepo = new UserRepositoryImpl(sessionFactory);
        NoteRepository noteRepo = new NoteRepositoryImpl(sessionFactory);
        RoleRepository roleRepo = new RoleRepositoryImpl(sessionFactory);
        ProfileRepository profileRepo = new ProfileRepositoryImpl(sessionFactory);

        UserService userService = new UserServiceImpl(userRepo);
        NoteService noteService = new NoteServiceImpl(noteRepo, userRepo);
        RoleService roleService = new RoleServiceImpl(roleRepo, userRepo);
        ProfileService profileService = new ProfileServiceImpl(profileRepo, userRepo);

        userController = new UserControllerImpl(userService);
        noteController = new NoteControllerImpl(noteService);
        roleController = new RoleControllerImpl(roleService);
        profileController = new ProfileControllerImpl(profileService);
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
            session.createNativeMutationQuery("TRUNCATE TABLE notes CASCADE").executeUpdate();
            session.createNativeMutationQuery("TRUNCATE TABLE profiles CASCADE").executeUpdate();
            session.createNativeMutationQuery("TRUNCATE TABLE user_role CASCADE").executeUpdate();
            session.createNativeMutationQuery("TRUNCATE TABLE roles CASCADE").executeUpdate();
            session.createNativeMutationQuery("TRUNCATE TABLE users CASCADE").executeUpdate();
            tx.commit();
        }

        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logAppender = new ListAppender<>();
        logAppender.start();
        rootLogger.addAppender(logAppender);
    }

    @AfterEach
    void tearDown() {
        if (logAppender != null) {
            Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.detachAppender(logAppender);
        }
    }

    @Test
    void testCreateUser() {
        String input = input().add("1").add("1").add("Иван").add("ivan@example.com").add("30").add("0").add("0").build();
        runConsole(input);
        assertOutputContains("Пользователь создан! ID:");
    }

    @Test
    void testFindUserById() {
        UserResponse created = userController.createUser(new UserRequest("FindById", "find@ex.com", 25));
        String input = input().add("1").add("2").add(String.valueOf(created.id())).add("0").add("0").build();
        runConsole(input);
        assertOutputContains("Найден пользователь:");
        assertOutputContains("Имя: FindById");
    }

    @Test
    void testFindUserByEmail_Success() {
        userController.createUser(new UserRequest("EmailUser", "email@ex.com", 30));
        String input = input().add("1").add("3").add("email@ex.com").add("0").add("0").build();
        runConsole(input);
        assertOutputContains("Найден пользователь:");
        assertOutputContains("Email: email@ex.com");
    }

    @Test
    void testFindUserByEmail_NotFound() {
        String input = input().add("1").add("3").add("nonexistent@ex.com").add("0").add("0").build();
        runConsole(input);
        assertOutputContains("Ошибка: Пользователь с email nonexistent@ex.com не найден");
    }

    @Test
    void testListUsers() {
        userController.createUser(new UserRequest("Alice", "alice@ex.com", 25));
        userController.createUser(new UserRequest("Bob", "bob@ex.com", 30));
        String input = input().add("1").add("6").add("0").add("0").add("0").build();
        runConsole(input);
        assertOutputContains("Пользователи (страница");
        assertOutputContains("Alice");
        assertOutputContains("Bob");
    }

    @ParameterizedTest
    @CsvSource({
            "Новое имя, ,30",
            " ,new@example.com,30",
            " , ,35",
            "Новое имя,new@example.com,35",
            "Новое имя, ,",
            " ,new@example.com,"
    })
    void testUpdateUser_PartialUpdates(String newName, String newEmail, String newAgeStr) {
        UserResponse original = userController.createUser(new UserRequest("OriginalName", "original@example.com", 30));

        String expectedName = (newName == null || newName.isBlank()) ? original.name() : newName;
        String expectedEmail = (newEmail == null || newEmail.isBlank()) ? original.email() : newEmail;
        int expectedAge = (newAgeStr == null || newAgeStr.isBlank()) ? original.age() : Integer.parseInt(newAgeStr);

        InputBuilder builder = input().add("1").add("4").add(String.valueOf(original.id()));
        builder.add(newName == null || newName.isBlank() ? "" : newName);
        builder.add(newEmail == null || newEmail.isBlank() ? "" : newEmail);
        builder.add(newAgeStr == null || newAgeStr.isBlank() ? "" : newAgeStr);
        builder.add("0").add("0");
        String consoleInput = builder.build();

        runConsole(consoleInput);
        assertOutputContains("Пользователь с ID " + original.id() + " обновлён");

        UserResponse updated = userController.findUserById(original.id());
        assertEquals(expectedName, updated.name());
        assertEquals(expectedEmail, updated.email());
        assertEquals(expectedAge, updated.age());
    }

    @Test
    void testDeleteUser() {
        UserResponse created = userController.createUser(new UserRequest("ToDelete", "delete@ex.com", 40));
        String input = input().add("1").add("5").add(String.valueOf(created.id())).add("y").add("0").add("0").build();
        runConsole(input);
        assertOutputContains("Пользователь с ID " + created.id() + " удалён");
        assertThrows(Exception.class, () -> userController.findUserById(created.id()));
    }

    private long createTestUserForNotes() {
        return createTestUser();
    }

    @Test
    void testCreateNote() {
        long userId = createTestUserForNotes();
        String input = input().add("2").add("1").add(String.valueOf(userId)).add("Содержимое заметки").add("0").add("0").build();
        runConsole(input);
        assertOutputContains("Заметка создана");
    }

    @Test
    void testFindNoteById() {
        long userId = createTestUserForNotes();
        NoteResponse note = noteController.createNote(userId, new NoteRequest("Найти меня"));
        String input = input().add("2").add("2").add(String.valueOf(note.id())).add("0").add("0").build();
        runConsole(input);
        assertOutputContains("Найдена заметка:");
        assertOutputContains("Найти меня");
    }

    @Test
    void testListNotes() {
        long userId = createTestUserForNotes();
        noteController.createNote(userId, new NoteRequest("Первая"));
        noteController.createNote(userId, new NoteRequest("Вторая"));
        String input = input().add("2").add("3").add(String.valueOf(userId)).add("0").add("0").add("0").build();
        runConsole(input);
        assertOutputContains("Заметки пользователя ID");
        assertOutputContains("Первая");
        assertOutputContains("Вторая");
    }

    @ParameterizedTest
    @CsvSource({
            "Новое содержимое, true",
            "  , false"
    })
    void testUpdateNote(String newContent, boolean shouldChange) {
        long userId = createTestUserForNotes();
        NoteResponse note = noteController.createNote(userId, new NoteRequest("Старое содержимое"));
        InputBuilder builder = input().add("2").add("4").add(String.valueOf(note.id()));
        builder.add(newContent == null || newContent.isBlank() ? "" : newContent);
        builder.add("0").add("0");
        runConsole(builder.build());

        if (shouldChange) {
            assertOutputContains("Заметка ID " + note.id() + " обновлена");
            NoteResponse updated = noteController.findNoteById(note.id());
            assertEquals(newContent, updated.content());
        }
    }

    @Test
    void testDeleteNote() {
        long userId = createTestUserForNotes();
        NoteResponse note = noteController.createNote(userId, new NoteRequest("Удалить"));
        String input = input().add("2").add("5").add(String.valueOf(note.id())).add("y").add("0").add("0").build();
        runConsole(input);
        assertOutputContains("Заметка с ID " + note.id() + " удалена");
        assertThrows(Exception.class, () -> noteController.findNoteById(note.id()));
    }

    @Test
    void testCreateRole() {
        String uniqueName = "Администратор_" + System.currentTimeMillis();
        String input = input().add("3").add("1").add(String.valueOf(System.currentTimeMillis())).add(uniqueName).add("0").add("0").build();
        runConsole(input);
        assertOutputContains("Создана роль: ID=");
        assertOutputContains("Название=" + uniqueName);
    }

    @Test
    void testFindRoleById() {
        String uniqueName = "Модератор_" + System.currentTimeMillis();
        RoleResponse role = roleController.createRole(new RoleRequest(System.currentTimeMillis(), uniqueName));
        String input = input().add("3").add("2").add(String.valueOf(role.id())).add("0").add("0").build();
        runConsole(input);
        assertOutputContains("Найдена роль:");
        assertOutputContains("Название: " + uniqueName);
    }

    @Test
    void testListRoles() {
        String role1 = "Роль1_" + System.currentTimeMillis();
        String role2 = "Роль2_" + System.currentTimeMillis();
        roleController.createRole(new RoleRequest(System.currentTimeMillis(), role1));
        roleController.createRole(new RoleRequest(System.currentTimeMillis() + 1, role2));
        String input = input().add("3").add("3").add("0").add("0").add("0").build();
        runConsole(input);
        assertOutputContains("Роли (страница");
        assertOutputContains(role1);
        assertOutputContains(role2);
    }

    @ParameterizedTest
    @CsvSource({
            "Новая роль, true",
            "  , false"
    })
    void testUpdateRole(String newName, boolean shouldChange) {
        String uniqueName = "Старая роль_" + System.currentTimeMillis();
        RoleResponse role = roleController.createRole(new RoleRequest(System.currentTimeMillis(), uniqueName));
        InputBuilder builder = input().add("3").add("4").add(String.valueOf(role.id()));
        builder.add(newName == null || newName.isBlank() ? "" : newName);
        builder.add("0").add("0");
        runConsole(builder.build());

        if (shouldChange) {
            assertOutputContains("Роль ID " + role.id() + " обновлена");
            RoleResponse updated = roleController.findRoleById(role.id());
            assertEquals(newName, updated.name());
        }
    }

    @Test
    void testDeleteRole() {
        String uniqueName = "Удаляемая роль_" + System.currentTimeMillis();
        RoleResponse role = roleController.createRole(new RoleRequest(System.currentTimeMillis(), uniqueName));
        String input = input().add("3").add("5").add(String.valueOf(role.id())).add("y").add("0").add("0").build();
        runConsole(input);
        assertOutputContains("Роль с ID " + role.id() + " удалена");
        assertThrows(Exception.class, () -> roleController.findRoleById(role.id()));
    }

    @Test
    void testAssignAndRemoveRoleToUser() {
        long userId = createTestUserForNotes();
        String uniqueName = "USER_ROLE_" + System.currentTimeMillis();
        RoleResponse role = roleController.createRole(new RoleRequest(System.currentTimeMillis(), uniqueName));

        String assignInput = input().add("3").add("6").add(String.valueOf(userId)).add(String.valueOf(role.id())).add("0").add("0").build();
        runConsole(assignInput);
        assertOutputContains("Роль ID " + role.id() + " назначена пользователю ID " + userId);

        logAppender.list.clear();
        String removeInput = input().add("3").add("7").add(String.valueOf(userId)).add(String.valueOf(role.id())).add("y").add("0").add("0").build();
        runConsole(removeInput);
        assertOutputContains("Роль ID " + role.id() + " снята с пользователя ID " + userId);
    }

    @Test
    void testCreateProfile() {
        long userId = createTestUserForNotes();
        String input = input().add("4").add("1").add(String.valueOf(userId)).add("+79991234567").add("Moscow").add("0").add("0").build();
        runConsole(input);
        assertOutputContains("Профиль создан для пользователя ID " + userId);
    }

    @Test
    void testGetProfile() {
        long userId = createTestUserForNotes();
        profileController.createProfile(userId, new ProfileRequest("+79990001122", "SPb"));
        String input = input().add("4").add("3").add(String.valueOf(userId)).add("0").add("0").build();
        runConsole(input);
        assertOutputContains("Профиль пользователя ID " + userId);
        assertOutputContains("Телефон: +79990001122");
        assertOutputContains("Адрес: SPb");
    }

    @ParameterizedTest
    @CsvSource({
            "+79998887766, NewCity, true, true",
            "  , NewCity, false, true",
            "+79998887766,  , true, false"
    })
    void testUpdateProfile(String phone, String address, boolean changePhone, boolean changeAddress) {
        long userId = createTestUserForNotes();
        profileController.createProfile(userId, new ProfileRequest("+70000000000", "OldCity"));

        InputBuilder builder = input().add("4").add("2").add(String.valueOf(userId));
        builder.add(phone == null || phone.isBlank() ? "" : phone);
        builder.add(address == null || address.isBlank() ? "" : address);
        builder.add("0").add("0");
        runConsole(builder.build());

        assertOutputContains("Профиль обновлён для пользователя ID " + userId);

        ProfileResponse updated = profileController.findProfileByUserId(userId);
        if (changePhone) assertEquals(phone, updated.phone());
        else assertEquals("+70000000000", updated.phone());
        if (changeAddress) assertEquals(address, updated.address());
        else assertEquals("OldCity", updated.address());
    }

    @Test
    void testDeleteProfile() {
        long userId = createTestUserForNotes();
        profileController.createProfile(userId, new ProfileRequest("+79991234567", "City"));
        String input = input().add("4").add("4").add(String.valueOf(userId)).add("y").add("0").add("0").build();
        runConsole(input);
        assertOutputContains("Профиль пользователя ID " + userId + " удалён");
        assertThrows(Exception.class, () -> profileController.findProfileByUserId(userId));
    }
}
