package com.crud.util;

import com.crud.exception.DatabaseConnectionException;
import com.crud.exception.DataAccessException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Утилитарный класс для управления Hibernate SessionFactory.
 * <p>
 * Реализует паттерн Singleton через статическую инициализацию.
 * SessionFactory создаётся один раз при загрузке класса и доступен через {@link #getSessionFactory()}.
 * </p>
 * <p>
 * При создании фабрики используется файл конфигурации {@code hibernate.cfg.xml},
 * который должен находиться в classpath ({@code src/main/resources}).
 * </p>
 * <p>
 * Перед созданием SessionFactory автоматически выполняется SQL-скрипт {@code db/schema.sql}
 * (расположенный в classpath), который создаёт необходимые таблицы.
 * Это позволяет использовать режим валидации схемы {@code hbm2ddl.auto = validate}
 * без ошибок отсутствия таблиц.
 * </p>
 * <p>
 * Параметры подключения к базе данных могут быть переопределены через переменные окружения:
 * <ul>
 *     <li>{@code DB_URL}    – JDBC URL (по умолчанию {@code jdbc:postgresql://localhost:5432/userdb})</li>
 *     <li>{@code DB_USER}   – имя пользователя (по умолчанию {@code postgres})</li>
 *     <li>{@code DB_PASSWORD} – пароль (по умолчанию {@code postgres})</li>
 * </ul>
 * </p>
 * <p>
 * В случае ошибки инициализации логируется сообщение и выбрасывается {@link ExceptionInInitializerError},
 * что предотвращает загрузку класса и дальнейшие попытки работы с БД.
 * </p>
 * <p>
 * Для корректного завершения приложения рекомендуется вызывать {@link #shutdown()} при остановке.
 * </p>
 *
 * @author charset-8utf
 * @version 2.0
 * @since 2026-04-24
 */
public class HibernateUtil {
    private static final Logger log = LoggerFactory.getLogger(HibernateUtil.class);
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private HibernateUtil() {
        throw new UnsupportedOperationException("Утилитарный класс, создание экземпляров запрещено");
    }

    /**
     * Возвращает значение переменной окружения или значение по умолчанию.
     *
     * @param name         имя переменной окружения
     * @param defaultValue значение по умолчанию
     * @return значение переменной, если она задана и не пуста, иначе defaultValue
     */
    private static String getEnvOrDefault(String name, String defaultValue) {
        return Optional.ofNullable(System.getenv(name))
                .filter(v -> !v.isBlank())
                .orElse(defaultValue);
    }

    /**
     * Возвращает JDBC URL для подключения к базе данных.
     *
     * @return JDBC URL (из переменной окружения {@code DB_URL} или значение по умолчанию)
     */
    private static String getDbUrl() {
        return getEnvOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/userdb");
    }

    /**
     * Возвращает имя пользователя для подключения к базе данных.
     *
     * @return имя пользователя (из переменной окружения {@code DB_USER} или значение по умолчанию)
     */
    private static String getDbUser() {
        return getEnvOrDefault("DB_USER", "postgres");
    }

    /**
     * Возвращает пароль для подключения к базе данных.
     *
     * @return пароль (из переменной окружения {@code DB_PASSWORD} или значение по умолчанию)
     */
    private static String getDbPassword() {
        return getEnvOrDefault("DB_PASSWORD", "postgres");
    }

    /**
     * Выполняет переданный SQL-скрипт через JDBC соединение.
     * <p>
     * Создаёт соединение, выполняет SQL и закрывает ресурсы.
     * При ошибке оборачивает {@link SQLException} в {@link DatabaseConnectionException}
     * с добавлением контекстной информации (URL, пользователь).
     * </p>
     *
     * @param url      JDBC URL
     * @param user     имя пользователя
     * @param password пароль
     * @param sql      текст скрипта (один SQL-оператор, без разделителей)
     * @throws DatabaseConnectionException если выполнение завершилось ошибкой
     */
    private static void executeSqlViaJdbc(String url, String user, String password, String sql) {
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new DatabaseConnectionException(
                    "Ошибка выполнения SQL-скрипта: " + e.getMessage() +
                            " [URL=" + url + ", user=" + user + "]", e);
        }
    }

    /**
     * Загружает и выполняет SQL-скрипт из classpath: {@code db/schema.sql}.
     * <p>
     * Скрипт должен содержать один SQL-оператор.
     * Если файл отсутствует или пуст, логируется предупреждение.
     * При ошибках чтения или выполнения выбрасывается {@link DatabaseConnectionException}.
     * </p>
     *
     * @throws DatabaseConnectionException если файл не найден, не может быть прочитан
     *                                     или выполнение SQL завершилось ошибкой
     */
    private static void executeSchemaScript() {
        String url = getDbUrl();
        String user = getDbUser();
        String password = getDbPassword();
        String scriptPath = "db/schema.sql";

        try (InputStream is = HibernateUtil.class.getClassLoader().getResourceAsStream(scriptPath)) {
            if (is == null) {
                throw new DatabaseConnectionException(
                        "Файл скрипта не найден: " + scriptPath + ". Проверьте путь src/main/resources/db/schema.sql", null);
            }
            String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
            if (sql.isEmpty()) {
                log.warn("SQL-скрипт пуст, таблицы не будут созданы");
                return;
            }
            executeSqlViaJdbc(url, user, password, sql);
        } catch (IOException e) {
            throw new DatabaseConnectionException(
                    "Не удалось прочитать файл скрипта " + scriptPath + ": " + e.getMessage(), e);
        }
    }

    /**
     * Формирует карту переопределений свойств Hibernate и HikariCP
     * на основе переменных окружения.
     *
     * @return карта (ключ - значение) для установки в конфигурацию Hibernate
     */
    private static Map<String, String> getConfigurationOverrides() {
        Map<String, String> overrides = new HashMap<>();
        String url = getDbUrl();
        String user = getDbUser();
        String password = getDbPassword();

        overrides.put("hibernate.connection.url", url);
        overrides.put("hibernate.connection.username", user);
        overrides.put("hibernate.connection.password", password);
        overrides.put("hibernate.hikari.jdbcUrl", url);
        overrides.put("hibernate.hikari.username", user);
        overrides.put("hibernate.hikari.password", password);
        overrides.put("hibernate.hikari.driverClassName", "org.postgresql.Driver");

        return overrides;
    }

    /**
     * Создаёт и настраивает Hibernate SessionFactory.
     * <p>
     * Выполняет следующие шаги:
     * <ol>
     *     <li>Выполняет SQL-скрипт {@code db/schema.sql} для создания таблиц через JDBC;</li>
     *     <li>Загружает конфигурацию из {@code hibernate.cfg.xml};</li>
     *     <li>Переопределяет параметры подключения из переменных окружения;</li>
     *     <li>Явно задаёт свойства для пула соединений HikariCP;</li>
     *     <li>Строит и возвращает {@link SessionFactory}.</li>
     * </ol>
     * </p>
     *
     * @return сконфигурированная SessionFactory
     * @throws DataAccessException         если произошла ошибка при создании SessionFactory
     * @throws DatabaseConnectionException если не удалось выполнить SQL-скрипт
     */
    private static SessionFactory buildSessionFactory() {
        try {
            executeSchemaScript();
            Configuration configuration = new Configuration().configure();
            getConfigurationOverrides().forEach(configuration::setProperty);
            return configuration.buildSessionFactory();
        } catch (Exception ex) {
            log.error("Критическая ошибка при создании SessionFactory (URL={}, user={})",
                    getDbUrl(), getDbUser(), ex);
            if (ex instanceof DatabaseConnectionException) {
                throw ex;
            } else {
                throw new DataAccessException(
                        "Не удалось создать SessionFactory. Причина: " + ex.getMessage(), ex);
            }
        }
    }

    /**
     * Возвращает экземпляр SessionFactory (единственный в приложении).
     *
     * @return SessionFactory (не null)
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Закрывает SessionFactory и освобождает все связанные ресурсы.
     * <p>
     * Рекомендуется вызывать этот метод при завершении приложения,
     * например, в блоке {@code finally}.
     * </p>
     */
    public static void shutdown() {
        Optional.ofNullable(sessionFactory)
                .filter(sf -> !sf.isClosed())
                .ifPresent(sf -> {
                    log.info("Закрытие SessionFactory");
                    sf.close();
                    log.info("SessionFactory закрыта успешно");
                });
    }
}