FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the pre-built JAR from your local target directory
COPY target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

# Restrict Java Heap to 256MB to leave 256MB for OS and ONNX native C++ engine
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -Xmx256m -Xms128m -XX:+UseSerialGC -jar app.jar"]