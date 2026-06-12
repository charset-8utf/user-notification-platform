import net.ltgt.gradle.errorprone.errorprone

plugins {
    `java-library`
    jacoco
    id("net.ltgt.errorprone") version "4.3.0"
}

configurations {
    testCompileClasspath {
        extendsFrom(configurations.compileOnly.get())
    }
    testRuntimeClasspath {
        extendsFrom(configurations.compileOnly.get())
    }
}

dependencies {
    api("org.springframework.boot:spring-boot-autoconfigure")
    api("io.micrometer:micrometer-core")
    api(libs.jspecify)
    api("io.swagger.core.v3:swagger-models-jakarta:2.2.30")
    implementation("org.springframework.boot:spring-boot-starter-zipkin")
    implementation("org.springframework.boot:spring-boot-starter-aspectj")
    compileOnly("org.springframework.boot:spring-boot-kafka")
    compileOnly("org.apache.kafka:kafka-clients")
    compileOnly("org.springframework.boot:spring-boot-configuration-processor")
    compileOnly("org.springframework.boot:spring-boot-micrometer-metrics")
    compileOnly("org.springframework.boot:spring-boot-starter-security")
    compileOnly("org.springframework.boot:spring-boot-starter-webmvc")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
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
    dependsOn(tasks.test)
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
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
