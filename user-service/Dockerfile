# Build
FROM maven:3.9.14-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/UserService-*.jar app.jar
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
ENTRYPOINT ["java", "-jar", "app.jar"]