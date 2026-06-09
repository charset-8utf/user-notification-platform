package com.notification.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

@ConfigurationProperties(prefix = "app.security.api-key")
public class ApiKeyProperties {

    private boolean enabled;
    private String keys = "";

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getKeys() {
        return keys;
    }

    public void setKeys(String keys) {
        this.keys = keys;
    }

    public List<String> resolvedKeys() {
        if (keys == null || keys.isBlank()) {
            return List.of();
        }
        return Arrays.stream(keys.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}
