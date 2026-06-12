package com.notification.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security.api-key")
public class ApiKeyProperties {

    private boolean enabled;
    private String keys = "";
    private String header = "X-API-Key";

    public List<String> resolvedKeys() {
        if (keys.isBlank()) {
            return List.of();
        }
        return Arrays.stream(keys.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}
