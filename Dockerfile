# syntax=docker/dockerfile:1

# Build the browser client with a relative API URL so it works behind the same
# host and port as the Spring Boot API.
FROM node:22-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# Package the API and include the compiled client as Spring Boot static files.
FROM maven:3.9.9-eclipse-temurin-21 AS backend-build
WORKDIR /app/backend
COPY backend/pom.xml ./
RUN mvn -B dependency:go-offline
COPY backend/ ./
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static
RUN mvn -B package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S veridian && adduser -S veridian -G veridian
COPY --from=backend-build /app/backend/target/veridian-it-agent-*.jar app.jar

USER veridian
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
