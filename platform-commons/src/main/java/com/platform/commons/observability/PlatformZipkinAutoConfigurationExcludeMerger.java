package com.platform.commons.observability;

import org.jspecify.annotations.Nullable;

public class PlatformZipkinAutoConfigurationExcludeMerger implements ZipkinAutoConfigurationExcludeMerger {

    @Override
    public String merge(@Nullable String existing) {
        if (existing == null || existing.isBlank()) {
            return ZIPKIN_AUTO_CONFIG;
        }
        if (existing.contains(ZIPKIN_AUTO_CONFIG)) {
            return existing;
        }
        return existing + "," + ZIPKIN_AUTO_CONFIG;
    }
}
