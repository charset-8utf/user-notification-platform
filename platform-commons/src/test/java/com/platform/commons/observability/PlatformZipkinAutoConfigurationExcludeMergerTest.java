package com.platform.commons.observability;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformZipkinAutoConfigurationExcludeMergerTest {

    private final PlatformZipkinAutoConfigurationExcludeMerger merger = new PlatformZipkinAutoConfigurationExcludeMerger();

    @Test
    void returnsZipkinConfigWhenExistingIsNull() {
        assertThat(merger.merge(null)).isEqualTo(ZipkinAutoConfigurationExcludeMerger.ZIPKIN_AUTO_CONFIG);
    }

    @Test
    void appendsZipkinConfigWhenOtherExcludesPresent() {
        assertThat(merger.merge("com.example.OtherAutoConfiguration"))
                .isEqualTo("com.example.OtherAutoConfiguration,"
                        + ZipkinAutoConfigurationExcludeMerger.ZIPKIN_AUTO_CONFIG);
    }

    @Test
    void doesNotDuplicateZipkinConfig() {
        String existing = "com.example.OtherAutoConfiguration,"
                + ZipkinAutoConfigurationExcludeMerger.ZIPKIN_AUTO_CONFIG;

        assertThat(merger.merge(existing)).isEqualTo(existing);
    }
}
