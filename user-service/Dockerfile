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
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=builder /build/target/UserService-1.0-SNAPSHOT.jar app.jar
USER appuser
ENTRYPOINT ["java", "-jar", "app.jar"]
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD java -cp app.jar com.crud.util.HealthCheck || exit 1