package com.crud.config;

import com.crud.security.ApiOutputSanitizer;
import com.crud.security.SanitizedJsonResponses;
import com.crud.security.SecurityJsonErrorWriter;
import com.platform.commons.observability.ExceptionMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Beans required by {@code SecurityConfig} / {@code JwtSecurityConfig}, {@code GlobalExceptionHandler},
 * and controllers that are not part of the default
 * {@link org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest} slice.
 */
@TestConfiguration
@Import({
        ApiOutputSanitizer.class,
        ApiAuthorizationRules.class,
        ApiHttpSecurityCustomizer.class,
        SecurityJsonErrorWriter.class,
        SanitizedJsonResponses.class
})
public class WebMvcTestSecuritySupport {

    @Bean
    ExceptionMetrics exceptionMetrics() {
        return new ExceptionMetrics(new SimpleMeterRegistry());
    }
}
