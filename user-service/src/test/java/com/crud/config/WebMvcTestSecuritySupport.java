package com.crud.config;

import com.crud.config.security.ApiAuthorizationRules;
import com.crud.config.security.ApiHttpSecurityCustomizer;
import com.crud.exception.ExceptionMessageResolver;
import com.crud.security.ApiOutputSanitizer;
import com.crud.security.SanitizedJsonResponses;
import com.crud.security.SecurityJsonErrorWriter;
import com.platform.commons.observability.ExceptionMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({
        ApiOutputSanitizer.class,
        ExceptionMessageResolver.class,
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
