FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY libs ./libs
COPY services/api-gateway ./services/api-gateway

RUN chmod +x gradlew && ./gradlew :services:api-gateway:bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
RUN addgroup -S unihub && adduser -S unihub -G unihub

COPY --from=builder /workspace/services/api-gateway/build/libs/*.jar app.jar
USER unihub

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+UseContainerSupport", "-jar", "app.jar"]
