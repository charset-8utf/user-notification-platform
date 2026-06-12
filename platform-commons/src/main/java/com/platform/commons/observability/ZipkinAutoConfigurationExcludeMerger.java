package com.platform.commons.observability;

import org.jspecify.annotations.Nullable;

/**
 * Strategy: дополняет {@code spring.autoconfigure.exclude} классом Zipkin auto-config.
 */
public interface ZipkinAutoConfigurationExcludeMerger {

    String ZIPKIN_AUTO_CONFIG = "org.springframework.boot.zipkin.autoconfigure.ZipkinAutoConfiguration";

    String merge(@Nullable String existing);
}
