package com.crud.util;

import org.junit.jupiter.api.Test;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Timeout;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
class HibernateUtilIntegrationTest {

    @Container
    private static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void hibernateUtil_WhenValidCredentials_ShouldSucceed() throws Exception {
        int exitCode = runHealthCheck(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        );
        assertEquals(0, exitCode, "SessionFactory должна создаться при корректных параметрах подключения");
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void hibernateUtil_WhenInvalidPassword_ShouldFail() throws Exception {
        int exitCode = runHealthCheck(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                "wrong_password"
        );
        assertEquals(1, exitCode, "HealthCheck должен завершиться с ошибкой при неверном пароле");
    }

    private int runHealthCheck(String dbUrl, String dbUser, String dbPassword) throws Exception {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + "/bin/java";
        String classpath = System.getProperty("java.class.path");

        ProcessBuilder processBuilder = new ProcessBuilder(
                javaBin, "-cp", classpath, "com.crud.util.HealthCheck"
        );
        processBuilder.environment().put("DB_URL", dbUrl);
        processBuilder.environment().put("DB_USER", dbUser);
        processBuilder.environment().put("DB_PASSWORD", dbPassword);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        String output = new BufferedReader(new InputStreamReader(process.getInputStream()))
                .lines().collect(Collectors.joining("\n"));
        int exitCode = process.waitFor();

        System.out.println("HealthCheck:\n" + output);

        return exitCode;
    }
}