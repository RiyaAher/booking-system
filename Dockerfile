# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring spring
COPY target/*.jar /app/app.jar

EXPOSE 8080
USER spring
ENTRYPOINT ["java","-jar","/app/app.jar"]
