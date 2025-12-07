# ==============================================================================
# Multi-stage Dockerfile for Consumer Finance Service (Production-Grade)
# ==============================================================================
# Stage 1: Build Stage
# Uses Maven to build the application and create an executable JAR
# ==============================================================================
FROM maven:3.9-amazoncorretto-17 AS builder

# Set working directory
WORKDIR /app

# Copy Maven configuration files first for better layer caching
# This allows Docker to cache dependencies if pom.xml hasn't changed
COPY pom.xml .

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds in production)
# Tests should be run in CI/CD pipeline before building the image
RUN mvn clean package -DskipTests -B

# ==============================================================================
# Stage 2: Runtime Stage
# Uses a minimal JRE image to run the application
# ==============================================================================
FROM amazoncorretto:17-alpine

# Add metadata labels
LABEL maintainer="consumer-finance-team"
LABEL application="consumer-finance-service"
LABEL version="1.0.0"
LABEL description="Spring Boot microservice for consumer lending operations"

# Install curl for health checks and debugging
RUN apk add --no-cache curl

# Create a non-root user for running the application (security best practice)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/consumer-finance-service-*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose the application port
EXPOSE 8080

# Configure JVM options for production
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/heap-dump.hprof"

# Health check configuration
# Checks if the application is responding on the health endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/v1/health || exit 1

# Run the application
# Using exec form ensures proper signal handling (SIGTERM for graceful shutdown)
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
