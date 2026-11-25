FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src src
RUN mvn --batch-mode --no-transfer-progress package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/wim100-backend-*.jar app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
