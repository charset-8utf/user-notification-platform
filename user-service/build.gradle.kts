import net.ltgt.gradle.errorprone.errorprone

plugins {
    alias(libs.plugins.spring.boot)
    jacoco
    alias(libs.plugins.errorprone)
}

group = "com.crud"
version = "1.0.0"

dependencies {
    implementation(project(":platform-commons"))
    implementation(project(":kafka-contracts"))
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j") {
        exclude(group = "io.github.resilience4j", module = "resilience4j-spring-boot3")
    }
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-restclient")
    implementation("org.springframework.boot:spring-boot-starter-kafka")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-aspectj")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework:spring-aspects")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("com.github.ben-manes.caffeine:jcache")
    implementation("org.apache.commons:commons-pool2")
    implementation(libs.spring.retry)
    implementation(libs.resilience4j.spring.boot4)
    implementation(libs.owasp.encoder)
    implementation(libs.postgresql)
    implementation(libs.hibernate.jcache)
    implementation(libs.springdoc.webmvc)
    implementation(libs.jspecify)
    implementation(libs.mapstruct)

    compileOnly("org.springframework.boot:spring-boot-configuration-processor")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor(libs.mapstruct.processor)
    annotationProcessor(libs.lombok.mapstruct.binding)

    runtimeOnly("org.springframework.boot:spring-boot-docker-compose")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-resttestclient")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-webmvc-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.kafka.module)
    testImplementation(libs.h2)
    testImplementation(libs.wiremock)

    errorprone(libs.error.prone.core)
    errorprone(libs.nullaway)
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
    archiveFileName.set("user-service.jar")
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
                minimum = "0.80".toBigDecimal()
            }
        }
        rule {
            element = "PACKAGE"
            includes = listOf("com.crud.*")
            excludes = listOf("com.crud", "com.crud.entity", "com.crud.util")
            limit {
                counter = "INSTRUCTION"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

tasks.named<Test>("test") {
    exclude("**/*IntegrationTest*")
    exclude("**/*E2ETest*")
    exclude("**/*ContractIntegrationTest*")
}

tasks.named<Test>("integrationTest") {
    maxParallelForks = 1
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

tasks.register<JavaExec>("serviceJwtSmokeToken") {
    group = "verification"
    description = "Print service JWT for smoke tests"
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("com.crud.support.ServiceJwtSmokeToken")
}
