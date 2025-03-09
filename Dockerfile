# Use a lightweight JDK image
FROM eclipse-temurin:21-jre-alpine

# Set work directory
WORKDIR /app

# Copy the JAR file
COPY target/*.jar app.jar

# Expose port
EXPOSE 8082

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
