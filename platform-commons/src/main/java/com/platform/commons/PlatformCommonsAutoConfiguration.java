package com.platform.commons;

import com.platform.commons.config.PlatformPropertiesConfiguration;
import com.platform.commons.kafka.KafkaSecurityPropertiesConfiguration;
import com.platform.commons.observability.ObservabilityAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Facade: точка входа автоконфигурации platform-commons.
 */
@AutoConfiguration
@Import({
        PlatformPropertiesConfiguration.class,
        KafkaSecurityPropertiesConfiguration.class,
        ObservabilityAutoConfiguration.class
})
public class PlatformCommonsAutoConfiguration {
}
