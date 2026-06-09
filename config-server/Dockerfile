# syntax=docker/dockerfile:1.7
# Build context: repository root. Requires: ./gradlew :config-server:bootJar -x test
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache wget
COPY config-server/build/libs/config-server.jar app.jar
EXPOSE 8888
HEALTHCHECK --interval=10s --timeout=3s --start-period=30s --retries=5 \
  CMD wget -q --spider http://localhost:8888/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
