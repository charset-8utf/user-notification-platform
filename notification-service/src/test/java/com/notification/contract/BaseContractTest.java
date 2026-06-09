package com.notification.contract;

import com.notification.service.NotificationService;
import com.notification.support.ServiceJwtTestSupport;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

/**
 * Базовый класс для тестов, сгенерированных Spring Cloud Contract (producer).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles({"contract", "rest"})
@Import(BaseContractTest.MetricsTestConfig.class)
public abstract class BaseContractTest {

    @TestConfiguration
    static class MetricsTestConfig {
        @Bean
        MeterRegistry contractMeterRegistry() {
            return new SimpleMeterRegistry();
        }
    }

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected NotificationService notificationService;

    @BeforeEach
    void setUpContractMockMvc() {
        doNothing().when(notificationService).sendEmailNotification(any());
        RestAssuredMockMvc.mockMvc(mockMvc);
    }
}
