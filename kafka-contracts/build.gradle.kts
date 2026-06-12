plugins {
    `java-library`
    id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1"
}

avro {
    isCreateSetters.set(false)
    fieldVisibility.set("PUBLIC")
    stringType.set("String")
}

val syncAvroSchemas = tasks.register<Sync>("syncAvroSchemas") {
    from(rootProject.layout.projectDirectory.dir("schemas/avro"))
    into(layout.projectDirectory.dir("src/main/avro"))
}

tasks.matching { it.name == "generateAvroJava" }.configureEach {
    dependsOn(syncAvroSchemas)
}

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
}

dependencies {
    api("io.confluent:kafka-avro-serializer:7.4.0")
    api("org.apache.avro:avro:1.11.4")
    api("org.apache.kafka:kafka-clients")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}
