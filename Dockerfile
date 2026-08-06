# Stage 1: Build stage (Java 21 JDK)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy dependency files first for Docker layer caching
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code and build final executable jar
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime stage (Java 21 JRE)
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy built JAR from stage 1
COPY --from=build /app/target/*.jar app.jar

# Render injects the PORT environment variable at runtime
ENV PORT=8080
EXPOSE 8080

# Configure JVM options for cloud container environments
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -Xmx400m -jar app.jar"]

