# Unihub Service

Hệ thống backend microservices cho nền tảng quản lý đào tạo và học tập đại học **Unihub**, xây dựng bằng **Java 21**, **Spring Boot 3.4**, **Gradle multi-project** theo mô hình **Database per Service**.

## Kiến trúc & Các Service

| Module | Cổng | Database / Lưu trữ | Trách nhiệm chính |
|---|---|---|---|
| `libs:common-starter` | — | — | `ApiResponse`, `ErrorResponse`, `PageResponse`, `GlobalExceptionHandler`, `CorrelationIdFilter`, `ServiceLifecycleLogger`, cấu hình `logback-spring.xml` |
| `services:api-gateway` | `8080` | Redis (rate limit) | Spring Cloud Gateway, định tuyến, xác thực RS256 JWT, rate limit theo IP, CORS, gộp Swagger UI |
| `services:identity-service` | `8081` | `identity_db` (Postgres) | Đăng nhập email/mật khẩu, Google OAuth2, refresh token xoay vòng, đổi/quên mật khẩu (Mailpit), RBAC (`ADMIN`/`LECTURER`/`STUDENT`), CRUD & import CSV/XLSX user, audit log |
| `services:academic-service` | `8082` | `academic_db` (Postgres) | Quản lý Khoa, Ngành, Lớp hành chính, Học kỳ (chống chồng lấn, 1 kỳ hiện hành), Môn học & tiên quyết (chống vòng lặp), seed dữ liệu mẫu |

## Hạ tầng Local (Docker Compose)

File [docker-compose.yml](docker-compose.yml) khởi chạy 5 dịch vụ nền tảng với profile `infra`:
- **PostgreSQL 16 + `pgvector`** (`:5432`): 1 container chứa các DB riêng (`identity_db`, `academic_db`, `learning_db`, `assessment_db`, `notification_db`, `report_db`, `ai_db`).
- **Redis 7** (`:6379`): Rate limit và cache.
- **RabbitMQ 3.13 Management** (`:5672`, UI `:15672`): Message broker cho giao tiếp bất đồng bộ.
- **Silo (MinIO-compatible S3)** (`:9000`, Console `:9001`): Lưu trữ tệp.
- **Mailpit** (`:1025`, Web UI `:8025`): Bắt email test ở môi trường local.

## Cấu trúc thư mục

```text
Unihub-service/
├── .github/workflows/       # CI (gitleaks, spotless, test, docker build) & CD (push GHCR)
├── deploy/
│   ├── certs/               # Chứng chỉ mkcert cho Traefik HTTPS local
│   ├── postgres/            # Script init-databases.sh tạo 7 DB + extension pgvector
│   ├── traefik/             # Cấu hình TLS động cho Traefik
│   ├── start-all.sh         # Bật hạ tầng + build + chạy nền toàn bộ service
│   ├── stop-all.sh          # Tắt toàn bộ service + hạ tầng
│   ├── deploy-local.sh      # Kéo image GHCR và chạy compose.prod.yml
│   └── rollback-local.sh    # Rollback về tag commit SHA trước
├── libs/common-starter/     # Thư viện dùng chung cho các Spring Boot service
├── services/
│   ├── api-gateway/
│   ├── identity-service/
│   └── academic-service/
├── compose.prod.yml         # Chạy bản production-like từ GHCR + Traefik (*.unihub.localhost)
├── docker-compose.yml       # Hạ tầng Docker local
├── Makefile                 # Lệnh tiện ích local
└── settings.gradle.kts
```

## Cấu hình & Khởi chạy nhanh

1. Tạo file biến môi trường từ mẫu:
   ```bash
   cp .env.example .env
   ```
   *(Nếu test đăng nhập Google OAuth2, điền thêm `GOOGLE_CLIENT_ID` và `GOOGLE_CLIENT_SECRET` vào `.env` với redirect URI `http://localhost:8080/login/oauth2/code/google`).*

2. Bật toàn bộ hạ tầng và 3 backend service bằng 1 lệnh:
   ```bash
   make start-all
   ```

3. Các lệnh tiện ích khác:
   ```bash
   make status-all   # Xem trạng thái Docker và PID của từng service
   make logs-all     # Theo dõi log tiếng Việt của các service (logs/*.log)
   make stop-all     # Tắt toàn bộ service và hạ tầng Docker
   make up-infra     # Chỉ bật hạ tầng Docker (khi chạy service trực tiếp từ IDE)
   make check        # Chạy Spotless format check và toàn bộ unit/RBAC tests
   make format       # Tự động format code theo chuẩn Spotless
   make reset-db     # Xóa volume DB và khởi tạo lại từ đầu
   ```

## Endpoint & Tài liệu API

- **Swagger UI (gộp qua Gateway):** `http://localhost:8080/swagger-ui.html`
- **Auth (`/api/v1/auth`):** `login`, `refresh`, `logout`, `change-password`, `forgot-password`, `reset-password`, `me`, `public-key`, và Google OAuth2 (`/oauth2/authorization/google`).
- **Admin Users (`/api/v1/users`):** CRUD user (tìm kiếm, lọc, phân trang, xóa mềm), khóa/mở khóa (`/lock`, `/unlock`), reset mật khẩu (`/reset-password`), import Excel/CSV (`/import/template`, `/import/preview`, `/import`).
- **Audit Log (`/api/v1/audit-logs`):** Tra cứu nhật ký thao tác hệ thống.
- **Academic (`/api/v1/academic`):** CRUD `faculties`, `majors`, `admin-classes` (kèm gán sinh viên), `semesters`, `subjects`.

## CI/CD & Chạy Production-like Local

- **CI (`.github/workflows/ci.yml`):** Tự động quét secret (`gitleaks`), chạy `./gradlew spotlessCheck check` và kiểm tra build `Dockerfile` của từng service khi mở PR hoặc push vào `main`/`develop`.
- **CD (`.github/workflows/cd.yml`):** Tự động build và đẩy image lên **GHCR (`ghcr.io`)** theo `commit SHA` và `latest` khi merge vào `main`.
- **Chạy production-like local:** Đặt `GHCR_OWNER=<github-username>` trong `.env`, chạy `./deploy/deploy-local.sh` (hỗ trợ HTTPS `https://api.unihub.localhost` khi sinh cert `mkcert` vào `deploy/certs/`).
