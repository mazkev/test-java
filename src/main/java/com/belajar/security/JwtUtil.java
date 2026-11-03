package com.belajar.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    // Kunci rahasia untuk menandatangani JWT (Standar HMAC-SHA256 minimal 256-bit)
    private static final String SECRET_STRING = "kunci-rahasia-marketplace-skala-besar-super-aman-2026";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));
    
    // Masa berlaku token: 24 Jam (dalam milidetik)
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;

    // 1. Membuat Token JWT baru saat user berhasil login
    public String generateToken(String email, String role, Long userId) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    // 2. Membaca isi payload (Claims) di dalam Token
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 3. Mengambil email dari token
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    // 4. Memeriksa apakah token masih aktif (belum expired)
    public boolean isTokenValid(String token) {
        try {
            return extractClaims(token).getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
