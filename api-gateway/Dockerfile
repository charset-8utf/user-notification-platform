# syntax=docker/dockerfile:1.7
# Build context: repository root. Requires: ./gradlew :api-gateway:bootJar -x test
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache wget
COPY api-gateway/build/libs/api-gateway.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --start-period=45s --retries=5 \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
