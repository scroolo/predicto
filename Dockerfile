# Stage 1 — Build frontend
FROM node:20-alpine AS frontend-builder
WORKDIR /frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ .
ARG VITE_API_BASE_URL=
ENV VITE_API_BASE_URL=
RUN npm run build

# Stage 2 — Build backend
FROM eclipse-temurin:21-jdk AS backend-builder
WORKDIR /app
COPY backend/pom.xml .
COPY backend/src ./src
COPY --from=frontend-builder /frontend/dist ./src/main/resources/static
RUN apt-get update && apt-get install -y maven
RUN mvn clean package -DskipTests
RUN jar tf target/predicto-0.0.1-SNAPSHOT.jar | grep "static" | head -5 || echo "NO STATIC FILES FOUND"

# Stage 3 — Run
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend-builder /app/target/predicto-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
