FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/web-bff-*.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]
