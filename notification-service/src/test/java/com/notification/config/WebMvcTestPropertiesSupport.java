package com.notification.config;

import com.notification.security.ApiKeyProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;

/** Properties-бины для {@code @WebMvcTest}, где нет {@link NotificationPropertiesConfiguration}. */
@TestConfiguration
@EnableConfigurationProperties({NotificationApiProperties.class, ApiKeyProperties.class})
public class WebMvcTestPropertiesSupport {
}
