import net.ltgt.gradle.errorprone.errorprone

plugins {
    alias(libs.plugins.spring.boot)
    jacoco
    alias(libs.plugins.errorprone)
}

group = "com.notification"
version = "1.0.0"

dependencies {
    implementation(project(":platform-commons"))
    implementation(project(":kafka-contracts"))
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.apache.commons:commons-pool2")
    implementation("org.springframework.boot:spring-boot-starter-kafka")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation(libs.springdoc.webmvc)
    implementation(libs.jspecify)
    implementation(libs.mapstruct)
    annotationProcessor(libs.mapstruct.processor)
    annotationProcessor(libs.lombok.mapstruct.binding)
    errorprone(libs.error.prone.core)
    errorprone(libs.nullaway)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-webmvc-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation(libs.testcontainers.mongodb)
    testImplementation(libs.testcontainers.kafka)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.greenmail)
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(
        listOf(
            "-Amapstruct.defaultComponentModel=spring",
            "-Amapstruct.unmappedTargetPolicy=ERROR",
            "-Amapstruct.suppressGeneratorTimestamp=true",
            "-Amapstruct.suppressGeneratorVersionInfoComment=true",
        )
    )
    options.errorprone {
        disableAllChecks.set(true)
        error("NullAway")
        option("NullAway:OnlyNullMarked", "true")
        excludedPaths.set(".*/build/generated/sources/annotationProcessor/.*")
    }
    if (name.contains("Test", ignoreCase = true)) {
        options.errorprone {
            disable("NullAway")
        }
    }
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName.set("notification-service.jar")
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

tasks.named<Test>("test") {
    exclude("**/*IntegrationTest*")
    exclude("**/*E2ETest*")
}

tasks.named<Test>("integrationTest") {
    maxParallelForks = 1
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}
