# Use OpenJDK base image
FROM eclipse-temurin:17-jdk

# Set the working directory
WORKDIR /app

# Copy everything (including mvnw, pom.xml, etc.)
COPY . .

# Ensure mvnw is executable
RUN chmod +x mvnw

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose the port your app runs on
EXPOSE 8080

# Run the JAR file (replace with actual jar name if needed)
CMD ["java", "-jar", "target/ReadMe-0.0.1-SNAPSHOT.jar"]
