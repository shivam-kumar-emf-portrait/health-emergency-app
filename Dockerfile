# Multi-stage Dockerfile for Lifeline Spring Boot Platform
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY backend/pom.xml backend/
COPY backend/src backend/src/
RUN mvn clean package -DskipTests

# Stage 2: Production Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/backend/target/health-emergency-backend-1.0.0.jar app.jar
EXPOSE 8080
ENV PORT=8080
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]
