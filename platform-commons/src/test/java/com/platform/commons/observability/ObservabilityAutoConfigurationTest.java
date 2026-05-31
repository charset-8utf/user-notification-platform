package com.platform.commons.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer;

import static org.assertj.core.api.Assertions.assertThat;

class ObservabilityAutoConfigurationTest {

    @Test
    void appliesCommonTags() {
        MeterRegistry registry = new SimpleMeterRegistry();
        ObservabilityAutoConfiguration config = new ObservabilityAutoConfiguration();
        MeterRegistryCustomizer<MeterRegistry> customizer =
                config.platformCommonTags("test-service", "unit-test");
        customizer.customize(registry);

        registry.counter("platform.test").increment();

        Counter counter = registry.find("platform.test").counter();
        assertThat(counter).isNotNull();

        Meter.Id meterId = counter.getId();
        assertThat(meterId).isNotNull();
        assertThat(meterId.getTag("application")).isEqualTo("test-service");
        assertThat(meterId.getTag("environment")).isEqualTo("unit-test");
    }
}
