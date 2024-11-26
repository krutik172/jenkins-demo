# Use a base image with Java runtime environment
FROM openjdk:21-jdk-slim

# Set a working directory inside the container
WORKDIR /app

# Copy the built application JAR file from the local machine to the container
COPY target/jenkins-pipeline-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port (default for Spring Boot is 8080)
EXPOSE 8089

# Define the command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
