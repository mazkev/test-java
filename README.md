# 🛒 Enterprise Marketplace Distributed Backend (Java 21 & Spring Boot 3.3.4)

[![CI/CD Pipeline](https://github.com/mazkev/test-java/actions/workflows/ci.yml/badge.svg)](https://github.com/mazkev/test-java/actions/workflows/ci.yml)
[![Java Version](https://img.shields.io/badge/Java-21%20LTS%20(Virtual%20Threads)-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Oracle Database](https://img.shields.io/badge/Database-Oracle%2023ai%20Free-F80000?logo=oracle&logoColor=white)](https://www.oracle.com/database/free/)
[![Redis](https://img.shields.io/badge/Distributed%20Cache-Redis%207-DC382D?logo=redis&logoColor=white)](https://redis.io/)
[![RabbitMQ](https://img.shields.io/badge/Message%20Broker-RabbitMQ%203%20Management-FF6600?logo=rabbitmq&logoColor=white)](https://www.rabbitmq.com/)
[![Zipkin APM](https://img.shields.io/badge/Tracing-OpenZipkin%20APM-008080?logo=openzipkin&logoColor=white)](https://zipkin.io/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

Backend REST API sistem marketplace skala enterprise (*high-traffic, fault-tolerant, resilient, zero-data-loss*) yang dibangun dari nol menggunakan **Java 21 Virtual Threads** dan **Spring Boot 3.3.4**. Mengadopsi standar arsitektur terdistribusi kelas industri (*banking & top-tier e-commerce*).

---

## 🏛️ Arsitektur Sistem Terdistribusi

```mermaid
graph TD
    Client[Client: Web / Mobile / REST Client] --> RateLimit[Token Bucket Rate Limiter]
    RateLimit --> CorrelationID[Correlation ID Filter & MDC Logging]
    CorrelationID --> SecurityGateway[Spring Security 6 + JWT RBAC]
    
    subgraph Docker Network (jv_default)
        subgraph App Pod (marketplace-backend : 8080)
            SecurityGateway --> ProductController[Products CRUD & Paging]
            SecurityGateway --> AuthController[Auth Register / Login JWT]
            SecurityGateway --> OrderController[Checkout & Concurrency Control]
            
            OrderController -->|Producer AMQP JSON| RabbitMQBroker
            RabbitMQBroker -->|Consumer @RabbitListener| OrderConsumer
            OrderController -->|Circuit Breaker & Fallback| Resilience4j[PaymentGateway Resilience4j]
        end

        subgraph Distributed Broker (marketplace-rabbitmq : 5672 / 15672)
            RabbitMQBroker[order.exchange] --> Queue[(order.invoice.queue)]
            Queue -.->|Failure 3x / Exponential Backoff| DLX[order.dlx.exchange]
            DLX --> DLQ[(order.invoice.dlq)]
        end

        subgraph Distributed Cache (marketplace-redis : 6379)
            ProductController <-->|Spring Cache @Cacheable / @CacheEvict JSON| RedisStore[(Redis Key-Value Cache)]
        end

        subgraph Enterprise Database (marketplace-oracle : 1521)
            ProductController --> OracleDB[(Oracle Database 23ai Free)]
            OrderController --> OracleDB
            AuthController --> OracleDB
            Flyway[(Flyway Migrations V1 & V2)] --> OracleDB
        end
    end

    subgraph Observability & CI/CD
        Actuator[Spring Boot Actuator Probes] --> K8s[Kubernetes Liveness/Readiness]
        Zipkin[OpenZipkin Distributed Tracing : 9411]
        CICD[GitHub Actions CI/CD Pipeline]
        Testing[Automated Test Suite: JUnit 5, Mockito, MockMvc]
    end
```

---

## 📋 20 Pilar Arsitektur Enterprise (Selesai 100%)

| Level | Fase | Fitur Enterprise | Teknologi / Anotasi | Implementasi & Manfaat |
| :---: | :---: | :--- | :--- | :--- |
| **L1** | **01** | **Pagination & Sorting** | `Pageable`, `Page<Product>`, `Sort.by()` | Efisiensi query katalog produk tanpa membebani memori server. |
| **L1** | **02** | **Strict Bean Validation** | `jakarta.validation`, `@Valid`, `@NotBlank` | Menolak payload cacat di garis depan dengan HTTP `400 Bad Request`. |
| **L1** | **03** | **OpenAPI 3 / Swagger** | `springdoc-openapi-starter-webmvc-ui` | Dokumentasi API interaktif visual di `/swagger-ui.html`. |
| **L1** | **04** | **Optimistic Locking** | JPA `@Version private Long version;` | Mencegah *race condition* rebutan stok saat flash sale (HTTP `409 Conflict`). |
| **L2** | **05** | **Stateless Security & RBAC** | Spring Security 6, JJWT 0.12.6, BCrypt | Pemisahan hak akses ketat antara penjual (`SELLER`) dan pembeli (`BUYER`). |
| **L2** | **06** | **Database Migration** | `Flyway`, `ddl-auto=validate` | Version control skema SQL database (`V1__init_schema.sql`, `V2__seed_sample_data.sql`). |
| **L2** | **07** | **In-Memory Caching** | Spring `@EnableCaching`, `@Cacheable` | Isolasi query repetitif agar data katalog terakses dalam sub-milidetik. |
| **L2** | **08** | **In-App Event-Driven** | `ApplicationEventPublisher`, `@Async` | Eksekusi background tugas asinkron memanfaatkan Java 21 Virtual Threads. |
| **L3** | **09** | **Production Observability** | `spring-boot-starter-actuator`, Micrometer | Endpoint Kubernetes Liveness & Readiness probe (`/actuator/health`). |
| **L3** | **10** | **Rate Limiting / Anti-Spam** | Token Bucket, `ConcurrentHashMap` | Menangkal serangan DoS/brute force (HTTP `429 Too Many Requests`). |
| **L3** | **11** | **Enterprise Database** | Oracle Database 23ai Free, OJDBC11 | Database ACID enterprise banking-grade dengan data tersimpan permanen. |
| **L3** | **12** | **Full Containerization** | Multi-stage Dockerfile JRE 21, Compose | Menjalankan seluruh cluster backend dan database dalam satu jaringan terisolasi. |
| **L3** | **13** | **Distributed Caching (Redis)** | Redis 7 Alpine, Spring Data Redis | Cache tersentralisasi dengan serialisasi JSON (`products::1`) untuk multi-pod. |
| **L3** | **14** | **Distributed Message Broker** | RabbitMQ 3, TopicExchange, `@RabbitListener` | Antrean pesan transaksi asinkron terdistribusi dengan format DTO JSON. |
| **L4** | **15** | **Dead Letter Queue (DLQ)** | RabbitMQ DLX, `order.invoice.dlq` | Menangani pesan gagal setelah 3x retry tanpa ada data yang hilang (*zero data loss*). |
| **L4** | **16** | **Distributed Tracing (APM)** | Micrometer Tracing, OpenZipkin | Melacak satu alur transaksi melintasi HTTP, DB Oracle, dan RabbitMQ. |
| **L4** | **17** | **Circuit Breaker & Fallback** | Resilience4j, Spring AOP, `@CircuitBreaker` | Mengisolasi kegagalan vendor pembayaran dan mengalihkan ke transfer manual. |
| **L4** | **18** | **Centralized Logging & MDC** | `CorrelationIdFilter`, Logback MDC | Pelacakan log terstruktur dengan header respon `X-Correlation-ID`. |
| **L5** | **19** | **Automated Test Suite** | JUnit 5, Mockito 5, MockMvc, AssertJ | Pengujian otomatis Service, Controller, dan Circuit Breaker (8/8 tests pass). |
| **L5** | **20** | **Cloud CI/CD Pipeline** | GitHub Actions (`.github/workflows/ci.yml`) | Otomasi build, automated test, dan packaging container Docker di cloud. |

---

## 🛠️ Tech Stack & Dependensi Utama

- **Bahasa & Runtime**: Java 21 LTS (dengan fitur Virtual Threads aktif)
- **Framework Utama**: Spring Boot 3.3.4 (Spring MVC, Spring Data JPA, Spring Security, Spring AMQP)
- **Database & Migrasi**: Oracle Database 23ai Free, Flyway Migration Tool, HikariCP
- **Caching & Broker**: Redis 7 Alpine, RabbitMQ 3 Management Alpine
- **Ketangguhan & Observabilitas**: Resilience4j, Micrometer Tracing, OpenZipkin APM, Spring Boot Actuator
- **Testing**: JUnit Jupiter 5, Mockito 5, Spring MockMvc, AssertJ
- **DevOps**: Docker Multi-stage Build, Docker Compose, GitHub Actions

---

## 🚀 Panduan Menjalankan Sistem

### 1. Menjalankan Seluruh Cluster (Docker Compose)

Pastikan Docker Desktop aktif di komputer Anda, lalu jalankan:

```powershell
# Jalankan seluruh 5 kontainer (Backend, Oracle, Redis, RabbitMQ, Zipkin)
docker compose up -d --build
```

Periksa status seluruh kontainer:
```powershell
docker compose ps
```

### 2. Menjalankan Unit & Integration Test Lokal

```powershell
.\mvnw.cmd test
```

---

## 🌐 Dashboard & Endpoint Layanan

| Layanan / Dashboard | URL Akses | Kredensial Default |
| :--- | :--- | :--- |
| **Backend REST API** | `http://localhost:8080` | Token JWT Bearer |
| **Swagger OpenAPI 3 UI** | `http://localhost:8080/swagger-ui.html` | Publik |
| **RabbitMQ Management Dashboard** | `http://localhost:15672` | User: `guest` / Pass: `guest` |
| **OpenZipkin Distributed Tracing** | `http://localhost:9411` | Tanpa Password |
| **Kubernetes Health Probes** | `http://localhost:8080/actuator/health` | Status: `UP` |

---

## 🧪 Skenario Pengujian REST API (`api.http`)

Proyek ini dilengkapi dengan skenario pengujian komprehensif siap pakai menggunakan ekstensi **REST Client (VS Code)** pada file:
👉 [api.http](api.http)

Mencakup pengujian:
1. Registrasi & Login JWT (`SELLER` & `BUYER`)
2. Pengujian Role-Based Access Control (RBAC)
3. Upload Katalog & Pembelian Produk
4. Pengujian Optimistic Locking (Flash Sale Concurrency)
5. Uji Coba Token Bucket Rate Limiting (Anti-Spam HTTP 429)
6. Uji Coba Dead Letter Queue (DLQ) & Retry Mechanism
7. Uji Coba Resilience4j Circuit Breaker Fallback
8. Uji Coba Correlation ID & Trace ID

---

## 📄 Lisensi
Proyek ini dilisensikan di bawah lisensi [MIT](LICENSE).
