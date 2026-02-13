# ── Stage 1: Build ──────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy parent POM first to leverage layer caching
COPY pom.xml .
COPY user-service/pom.xml user-service/
COPY post-service/pom.xml post-service/

# Download dependencies (cached if POMs unchanged)
RUN mvn dependency:go-offline -pl post-service -am -q

# Copy source and build
COPY post-service/src post-service/src
RUN mvn clean package -pl post-service -am -DskipTests -q

# ── Stage 2: Runtime ─────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

# Add a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy the fat JAR from the builder stage
COPY --from=builder /build/post-service/target/*.jar app.jar

RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
