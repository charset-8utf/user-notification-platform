# syntax=docker/dockerfile:1.7
# Requires: ./gradlew :user-service:bootJar -x test
FROM eclipse-temurin:21-jre-alpine
LABEL org.opencontainers.image.title="user-service"
WORKDIR /app
RUN apk add --no-cache wget tini \
 && addgroup -S appgroup \
 && adduser -S appuser -G appgroup
COPY user-service/build/libs/user-service.jar app.jar
ENV JAVA_OPTS="-XX:InitialRAMPercentage=20.0 -XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8"
USER appuser
EXPOSE 8443 8081
ENTRYPOINT ["/sbin/tini", "--", "sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
HEALTHCHECK --interval=30s --timeout=3s --start-period=90s --retries=3 \
  CMD wget -q --spider http://127.0.0.1:8081/actuator/health || exit 1
