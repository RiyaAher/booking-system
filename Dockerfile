# Stage 1: Build stage with capped Maven heap size
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Limit Maven's heap usage so it fits within Render's build limits
ENV MAVEN_OPTS="-Xmx300m -XX:+UseSerialGC"

# Copy dependency files first
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code and build jar
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

# Tune JVM options for 512MB RAM container
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -Xmx300m -Xms128m -XX:+UseSerialGC -jar app.jar"]