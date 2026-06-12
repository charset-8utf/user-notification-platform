package com.crud.config;

import com.crud.config.kafka.AppKafkaProperties;
import com.crud.config.kafka.UserNotificationKafkaProperties;
import com.crud.config.ratelimit.RateLimitProperties;
import com.crud.config.rest.NotificationRestProperties;
import com.crud.config.rest.NotificationRestTlsProperties;
import com.crud.config.security.JwtProperties;
import com.crud.security.servicejwt.ServiceJwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        JwtProperties.class,
        ServiceJwtProperties.class,
        AppKafkaProperties.class,
        UserNotificationKafkaProperties.class,
        NotificationRestProperties.class,
        NotificationRestTlsProperties.class,
        RateLimitProperties.class
})
public class UserServicePropertiesConfiguration {
}
