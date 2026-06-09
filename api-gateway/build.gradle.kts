plugins {
    alias(libs.plugins.spring.boot)
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
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation(libs.wiremock)
}

tasks.named<Test>("test") {
    include("**/*Test.java", "**/*IntegrationTest.java")
}

tasks.named<Test>("integrationTest") {
    enabled = false
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName.set("api-gateway.jar")
}
