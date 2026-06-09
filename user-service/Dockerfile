# syntax=docker/dockerfile:1.7
FROM maven:3.9.14-eclipse-temurin-21 AS builder
WORKDIR /build

COPY pom.xml .
COPY platform-commons/pom.xml platform-commons/pom.xml
COPY user-service/pom.xml user-service/pom.xml

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -f pom.xml install -N \
    && mvn -B -f platform-commons/pom.xml dependency:go-offline -DskipTests

COPY platform-commons/src platform-commons/src
COPY user-service/src user-service/src

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -f platform-commons/pom.xml install -DskipTests \
    && mvn -B -f user-service/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre-alpine
LABEL org.opencontainers.image.title="user-service" \
      org.opencontainers.image.source="https://example.com/user-notification-platform" \
      org.opencontainers.image.licenses="MIT"
WORKDIR /app
RUN apk add --no-cache wget tini \
 && addgroup -S appgroup \
 && adduser -S appuser -G appgroup
COPY --from=builder /build/user-service/target/user-service-1.0.0.jar app.jar
ENV JAVA_OPTS="-XX:InitialRAMPercentage=20.0 -XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8"
USER appuser
EXPOSE 8443 8081
ENTRYPOINT ["/sbin/tini", "--", "sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget -q --spider http://127.0.0.1:8081/actuator/health 2>/dev/null \
  || wget -q --spider --no-check-certificate https://127.0.0.1:8443/actuator/health || exit 1
