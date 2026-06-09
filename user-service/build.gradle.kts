plugins {
    alias(libs.plugins.spring.boot)
    jacoco
}

group = "com.crud"
version = "1.0.0"

dependencies {
    implementation(project(":platform-commons"))
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    implementation(libs.spring.retry)
    implementation("org.springframework:spring-aspects")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation(libs.owasp.encoder)
    implementation(libs.postgresql)
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("com.github.ben-manes.caffeine:jcache")
    implementation(libs.hibernate.jcache)
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.springframework.boot:spring-boot-starter-restclient")
    implementation("org.springframework.boot:spring-boot-starter-kafka")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.apache.commons:commons-pool2")
    implementation("org.springframework.boot:spring-boot-starter-aspectj")
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j") {
        exclude(group = "io.github.resilience4j", module = "resilience4j-spring-boot3")
    }
    implementation(libs.resilience4j.spring.boot4)
    implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    compileOnly("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation(libs.jspecify)
    runtimeOnly("org.springframework.boot:spring-boot-docker-compose")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-resttestclient")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:kafka:1.20.6")
    testImplementation(libs.h2)
    testImplementation(libs.wiremock)
    testImplementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-webmvc-test")
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

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

tasks.register<JavaExec>("serviceJwtSmokeToken") {
    group = "verification"
    description = "Print service JWT for smoke tests"
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("com.crud.support.ServiceJwtSmokeToken")
}
