package com.crud;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke-тест для проверки загрузки контекста Spring.
 */
@SpringBootTest
@ActiveProfiles("test")
class UserServiceApplicationTest {

    @Test
    void contextLoads() {
    }
}
