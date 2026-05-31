package com.platform.commons;

import com.platform.commons.logging.LoggingConfiguration;
import com.platform.commons.observability.ObservabilityAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({
        LoggingConfiguration.class,
        ObservabilityAutoConfiguration.class
})
public class PlatformCommonsAutoConfiguration {
}
