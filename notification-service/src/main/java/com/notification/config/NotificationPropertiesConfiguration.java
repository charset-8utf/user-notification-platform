package com.notification.config;

import com.notification.config.kafka.AppKafkaProperties;
import com.notification.config.kafka.NotificationKafkaProperties;
import com.notification.security.ApiKeyProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        NotificationProperties.class,
        NotificationApiProperties.class,
        NotificationKafkaProperties.class,
        AppKafkaProperties.class,
        ApiKeyProperties.class
})
public class NotificationPropertiesConfiguration {
}
