# Multi-stage Dockerfile for Performance Optimization Service

# Stage 1: Build stage
FROM maven:3.9.4-eclipse-temurin-21 as builder

WORKDIR /build

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src/ src/

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /build/target/Performance-Optimization-Service-1.0-SNAPSHOT.jar app.jar

# Create logs directory
RUN mkdir -p logs

# Expose port
EXPOSE 8096

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8096/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
CMD ["--spring.profiles.active=prod"]

