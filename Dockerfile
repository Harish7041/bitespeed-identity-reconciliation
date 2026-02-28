# Use an official JDK 21 runtime as a parent image
FROM eclipse-temurin:21-jdk-alpine

# Set the working directory in the container
WORKDIR /app

# Copy the maven wrapper and pom file
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy the source code and build the application
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Run the jar file
ENTRYPOINT ["java","-jar","target/identity-reconciliation-0.0.1-SNAPSHOT.jar"]