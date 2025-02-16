# Build stage
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Install maven
RUN apt-get update && apt-get install -y maven

# Copy pom.xml for dependency caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN mvn package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the built artifact from build stage
COPY --from=build /app/target/*.jar app.jar

# Create a non-root user
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Set the entrypoint
ENTRYPOINT ["java", "-jar", "app.jar"]

# Document that the container listens on port 8080
EXPOSE 5000
