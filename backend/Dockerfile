# Build stage
FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

# Copy gradle files first for caching
COPY gradle.properties settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle

# Copy source code
COPY core ./core
COPY domain ./domain
COPY infrastructure ./infrastructure
COPY application ./application
COPY api ./api

# Build the application
RUN gradle :api:bootJar -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Add non-root user for security
RUN addgroup -S oms && adduser -S oms -G oms

# Copy the built jar
COPY --from=builder /app/api/build/libs/*.jar app.jar

# Set ownership
RUN chown -R oms:oms /app

USER oms

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
