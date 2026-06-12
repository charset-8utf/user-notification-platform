import net.ltgt.gradle.errorprone.errorprone

plugins {
    alias(libs.plugins.spring.boot)
    jacoco
    id("net.ltgt.errorprone") version "4.3.0"
}

dependencies {
    implementation(project(":platform-commons"))
    implementation("org.springframework.cloud:spring-cloud-starter-gateway-server-webflux")
    implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j") {
        exclude(group = "io.github.resilience4j", module = "resilience4j-spring-boot3")
    }
    implementation(libs.resilience4j.spring.boot4)
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation(libs.springdoc.webflux)
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation(libs.jspecify)
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation(libs.wiremock)
    errorprone("com.google.errorprone:error_prone_core:2.36.0")
    errorprone("com.uber.nullaway:nullaway:0.12.3")
}

tasks.withType<JavaCompile>().configureEach {
    options.errorprone {
        disableAllChecks.set(true)
        error("NullAway")
        option("NullAway:OnlyNullMarked", "true")
    }
    if (name.contains("Test", ignoreCase = true)) {
        options.errorprone {
            disable("NullAway")
        }
    }
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

tasks.jacocoTestReport {
    dependsOn(tasks.test, tasks.named("integrationTest"))
    executionData.setFrom(fileTree(layout.buildDirectory.dir("jacoco")).include("*.exec"))
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    executionData.setFrom(fileTree(layout.buildDirectory.dir("jacoco")).include("*.exec"))
    violationRules {
        rule {
            element = "BUNDLE"
            limit {
                counter = "INSTRUCTION"
                minimum = "0.70".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName.set("api-gateway.jar")
}
