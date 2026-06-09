plugins {
    `java-library`
}

dependencies {
    api("org.springframework.boot:spring-boot-autoconfigure")
    api("io.micrometer:micrometer-core")
    implementation("org.springframework.boot:spring-boot-starter-zipkin")
    implementation("org.springframework.boot:spring-boot-starter-aspectj")
    compileOnly("org.springframework.boot:spring-boot-configuration-processor")
    compileOnly("org.springframework.boot:spring-boot-micrometer-metrics")
    compileOnly("org.springframework.boot:spring-boot-starter-security")
    compileOnly("org.springframework.boot:spring-boot-starter-webmvc")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-micrometer-metrics")
}
