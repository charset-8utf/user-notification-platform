plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "user-notification-platform"

include(
    "platform-commons",
    "config-server",
    "api-gateway",
    "web-bff",
    "user-service",
    "notification-service",
)
