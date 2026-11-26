# Unihub Service

Backend microservices (Spring Boot 3, Java 21, Gradle multi-project).

## Khởi chạy hạ tầng local

```bash
cp .env.example .env
make up-infra
```

## Lệnh phát triển

```bash
./gradlew build
./gradlew spotlessApply
```
