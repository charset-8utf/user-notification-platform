package com.crud.config;

import com.crud.config.security.JwtProperties;
import com.crud.security.JwtRoleSupport;
import com.crud.security.JwtSecretKeyFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.time.Duration;

@TestConfiguration
@Import({JwtSecretKeyFactory.class, JwtRoleSupport.class})
public class WebMvcJwtTestSupport {

    @Bean
    JwtProperties jwtProperties() {
        return new JwtProperties(
                "0123456789012345678901234567890123456789012345678901234567890",
                "user-service",
                null,
                null,
                Duration.ofMinutes(15),
                Duration.ofDays(7));
    }
}
