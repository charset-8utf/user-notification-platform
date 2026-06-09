import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

plugins {
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    java
}

allprojects {
    group = "com.platform"
    version = "1.0.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")

    // Trivy: CVE в transitive deps (netty, tomcat, bouncycastle)
    extra["netty.version"] = "4.2.15.Final"
    extra["tomcat.version"] = "11.0.22"

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    extensions.configure<DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:4.0.6")
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.1.1")
        }
        dependencies {
            dependency("org.bouncycastle:bcprov-jdk18on:1.84")
            dependency("org.springframework.cloud:spring-cloud-config-server:5.0.3")
            dependency("io.github.resilience4j:resilience4j-spring-boot4:2.4.0")
            dependency("org.wiremock:wiremock-standalone:3.10.0")
            dependency("org.testcontainers:testcontainers-postgresql:2.0.5")
            dependency("org.testcontainers:testcontainers-junit-jupiter:2.0.5")
            dependency("org.testcontainers:testcontainers-mongodb:2.0.5")
            dependency("org.testcontainers:testcontainers-kafka:2.0.5")
            dependency("org.testcontainers:kafka:1.20.6")
        }
    }

    dependencies {
        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.named<Test>("test") {
        if (project.name !in setOf("user-service", "notification-service")) {
            exclude("**/*IntegrationTest*")
            exclude("**/*E2ETest*")
            exclude("**/*ContractIntegrationTest*")
        }
    }

    tasks.register<Test>("integrationTest") {
        description = "Runs integration tests."
        group = "verification"
        include("**/*IntegrationTest*")
        include("**/*E2ETest*")
        include("**/*ContractIntegrationTest*")
        shouldRunAfter(tasks.named("test"))
    }

    tasks.named("check") {
        dependsOn(tasks.named("integrationTest"))
    }
}
