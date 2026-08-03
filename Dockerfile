# Stage 1: Compile and package the Java application
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build

# Copy dependency definition and source code
COPY pom.xml .
COPY src ./src

# Build the jar file inside Docker
RUN mvn clean package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Set up non-root user for security
RUN groupadd --system spring && useradd --system --gid spring spring

# Copy the compiled jar from the builder stage
COPY --from=builder /build/target/*.jar /app/app.jar

EXPOSE 8080
USER spring

ENTRYPOINT ["java", "-jar", "/app/app.jar"]