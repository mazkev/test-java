# ========================================================
# STAGE 1: BUILDER (Maven 3.9 + JDK 21 Alpine)
# ========================================================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Salin pom.xml dan unduh dependensi
COPY pom.xml ./
RUN mvn dependency:resolve -B

# Salin seluruh kode sumber dan compile menjadi file .jar
COPY src ./src
RUN mvn clean package -DskipTests

# ========================================================
# STAGE 2: RUNNER (Image produksi super ringan ~150MB)
# ========================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Buat non-root user agar aman (Best Practice Keamanan Docker)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Salin hanya file .jar yang sudah jadi dari Stage 1
COPY --from=builder /app/target/my-api-1.0.0.jar app.jar

# Buka port 8080
EXPOSE 8080

# Jalankan aplikasi Java
ENTRYPOINT ["java", "-jar", "app.jar"]
