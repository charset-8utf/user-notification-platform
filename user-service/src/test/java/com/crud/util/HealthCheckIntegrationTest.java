package com.crud.util;

import org.junit.jupiter.api.Test;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
class HealthCheckIntegrationTest {

    @Container
    private static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Test
    void healthCheck_ShouldExitZeroWhenDatabaseAvailable() throws Exception {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + "/bin/java";
        String classpath = System.getProperty("java.class.path");

        ProcessBuilder processBuilder = new ProcessBuilder(
                javaBin, "-cp", classpath, "com.crud.util.HealthCheck"
        );
        processBuilder.environment().put("DB_URL", postgres.getJdbcUrl());
        processBuilder.environment().put("DB_USER", postgres.getUsername());
        processBuilder.environment().put("DB_PASSWORD", postgres.getPassword());
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        String output = new BufferedReader(new InputStreamReader(process.getInputStream()))
                .lines().collect(Collectors.joining("\n"));
        int exitCode = process.waitFor();

        assertEquals(0, exitCode, "Состояние HealthCheck: " + output);
    }
}