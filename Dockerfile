# Stage 1: Build stage (using Debian-based JDK for ONNX C++ library compatibility)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy dependency files first to leverage Docker layer caching
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code and build final executable jar
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime stage (Ubuntu/Debian based JRE to support ONNX native bindings)
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy built JAR from stage 1
COPY --from=build /app/target/*.jar app.jar

# Render injects the PORT environment variable at runtime
ENV PORT=8080
EXPOSE 8080

# Configure JVM options for cloud container environments
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -Xmx400m -jar app.jar"]