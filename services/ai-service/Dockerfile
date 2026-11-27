FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY libs ./libs
COPY services ./services

RUN chmod +x gradlew && ./gradlew :services:ai-service:bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
RUN addgroup -S unihub && adduser -S unihub -G unihub

COPY --from=builder /workspace/services/ai-service/build/libs/*.jar app.jar
USER unihub

EXPOSE 8087
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+UseContainerSupport", "-jar", "app.jar"]
