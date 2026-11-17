package com.belajar.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    // Stempel rahasia server (minimal 256-bit untuk standar HMAC-SHA256)
    private static final String SECRET = "belajar-java-spring-boot-super-secret-key-marketplace-2024!";
    private static final long EXPIRATION_TIME_MS = 24 * 60 * 60 * 1000; // Berlaku 24 Jam

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    // 1. Cetak Tiket JWT saat User Login
    public String generateToken(Long userId, String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
                .signWith(getSigningKey())
                .compact();
    }

    // 2. Baca isi stempel tiket
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public Long extractUserId(String token) {
        Object val = extractClaims(token).get("userId");
        return val != null ? ((Number) val).longValue() : null;
    }

    // 3. Validasi apakah tiket asli dan belum kedaluwarsa
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false; // Token palsu / rusak / expired
        }
    }
}