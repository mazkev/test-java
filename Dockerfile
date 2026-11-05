# ========================================================
# STAGE 1: BUILDER (Kompilasi kode sumber menjadi .jar)
# ========================================================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Salin Maven Wrapper dan pom.xml terlebih dahulu untuk caching dependensi
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Unduh dependensi Maven
RUN ./mvnw dependency:go-offline -B

# Salin seluruh kode sumber dan compile menjadi file .jar
COPY src ./src
RUN ./mvnw clean package -DskipTests

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