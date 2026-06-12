package com.platform.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.gateway.api")
public record GatewayApiProperties(
        @DefaultValue Actuator actuator,
        @DefaultValue Auth auth,
        @DefaultValue Admin admin,
        @DefaultValue UserApi user,
        @DefaultValue NotificationApi notification
) {

    public record Actuator(
            @DefaultValue("/actuator/health") String health,
            @DefaultValue("/actuator/info") String info,
            @DefaultValue("/actuator/prometheus") String prometheus
    ) {
    }

    public record Auth(
            @DefaultValue("/api/auth/login") String login,
            @DefaultValue("/api/auth/refresh") String refresh
    ) {
    }

    public record Admin(
            @DefaultValue("/api/roles/assign") String roleAssign,
            @DefaultValue("/api/roles/remove") String roleRemove
    ) {
    }

    public record UserApi(
            @DefaultValue("/api/users/**") String users,
            @DefaultValue("/api/profiles/**") String profiles,
            @DefaultValue("/api/roles/**") String roles
    ) {
    }

    public record NotificationApi(
            @DefaultValue("/api/notifications/logs/**") String logs,
            @DefaultValue("/api/notifications/**") String denyAll
    ) {
    }
}
