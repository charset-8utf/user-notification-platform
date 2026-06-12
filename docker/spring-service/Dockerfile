# syntax=docker/dockerfile:1
# Shared multi-stage build for all Spring Boot modules in the monorepo.
#
# Example:
#   docker build -f docker/spring-service/Dockerfile \
#     --build-arg MODULE=user-service \
#     --build-arg JAR_FILE=user-service.jar \
#     -t user-service:latest .

ARG MODULE
ARG JAR_FILE
ARG HEALTHCHECK_INTERVAL=30s
ARG HEALTHCHECK_START_PERIOD=90s
ARG HEALTHCHECK_SHELL="wget -q --spider http://127.0.0.1:8081/actuator/health/liveness || exit 1"

FROM gradle:8.14-jdk21-alpine AS builder
ARG MODULE
WORKDIR /home/gradle/project
USER root
COPY . .
RUN chown -R gradle:gradle /home/gradle/project
USER gradle
RUN gradle ":${MODULE}:bootJar" -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine
ARG MODULE
ARG JAR_FILE
ARG HEALTHCHECK_INTERVAL
ARG HEALTHCHECK_START_PERIOD
ARG HEALTHCHECK_SHELL

LABEL org.opencontainers.image.title="${MODULE}"
WORKDIR /app
RUN apk add --no-cache wget tini \
 && addgroup -S -g 1000 appgroup \
 && adduser -S -u 1000 -G appgroup appuser \
 && chown -R appuser:appgroup /app
COPY --from=builder --chown=appuser:appgroup /home/gradle/project/${MODULE}/build/libs/${JAR_FILE} app.jar
ENV JAVA_OPTS="-XX:InitialRAMPercentage=20.0 -XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8" \
    HEALTHCHECK_SHELL=${HEALTHCHECK_SHELL}
USER appuser
HEALTHCHECK --interval=${HEALTHCHECK_INTERVAL} --timeout=3s --start-period=${HEALTHCHECK_START_PERIOD} --retries=3 \
  CMD sh -c "${HEALTHCHECK_SHELL}"
ENTRYPOINT ["/sbin/tini", "--", "sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
