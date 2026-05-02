package com.crud;

import com.crud.exception.DataAccessException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.flywaydb.core.Flyway;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Утилита для управления Hibernate SessionFactory.
 */
@Slf4j
public class HibernateUtil {

    private final SessionFactory sessionFactory;

    public HibernateUtil() {
        this.sessionFactory = buildSessionFactory();
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    private static String getPropertyOrEnvOrDefault(String propertyName, String defaultValue) {
        return Optional.ofNullable(System.getProperty(propertyName))
                .filter(v -> !v.isBlank())
                .orElseGet(() -> Optional.ofNullable(System.getenv(propertyName))
                        .filter(v -> !v.isBlank())
                        .orElse(defaultValue));
    }

    private static String getDbUrl() {
        return getPropertyOrEnvOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/userdb");
    }

    private static String getDbUser() {
        return getPropertyOrEnvOrDefault("DB_USER", "postgres");
    }

    private static String getDbPassword() {
        return getPropertyOrEnvOrDefault("DB_PASSWORD", "postgres");
    }

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

    private static SessionFactory buildSessionFactory() {
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(getDbUrl(), getDbUser(), getDbPassword())
                    .locations("db/migration")
                    .load();
            flyway.migrate();

            Configuration configuration = new Configuration().configure();
            getConfigurationOverrides().forEach(configuration::setProperty);
            return configuration.buildSessionFactory();
        } catch (RuntimeException ex) {
            throw new DataAccessException(
                    "Не удалось создать SessionFactory (URL=" + getDbUrl() + ", user=" + getDbUser() + "). Причина: " + ex.getMessage(), ex);
        }
    }

    public void shutdown() {
        Optional.ofNullable(sessionFactory)
                .filter(sf -> !sf.isClosed())
                .ifPresent(sf -> {
                    log.info("Закрытие SessionFactory");
                    sf.close();
                    log.info("SessionFactory закрыта успешно");
                });
    }
}
