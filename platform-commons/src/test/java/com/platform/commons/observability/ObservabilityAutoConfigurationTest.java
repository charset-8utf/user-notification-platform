package com.platform.commons.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class ObservabilityAutoConfigurationTest {

    @Test
    void appliesCommonTagsFromResolver() {
        MeterRegistry registry = new SimpleMeterRegistry();
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.application.name", "test-service");
        EnvironmentTagResolver environmentTagResolver = () -> "staging";

        ObservabilityAutoConfiguration config = new ObservabilityAutoConfiguration();
        MeterRegistryCustomizer<MeterRegistry> customizer =
                config.platformCommonTags(environment, environmentTagResolver);
        customizer.customize(registry);

        registry.counter("platform.test").increment();

        Counter counter = registry.find("platform.test").counter();
        assertThat(counter).isNotNull();
        Meter.Id meterId = counter.getId();
        assertThat(meterId.getTag("application")).isEqualTo("test-service");
        assertThat(meterId.getTag("environment")).isEqualTo("staging");
    }
}
