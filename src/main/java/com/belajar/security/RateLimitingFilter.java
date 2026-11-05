package com.belajar.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    // Konfigurasi Batas: Maksimal 5 request per jendela 10 detik per IP
    private static final int MAX_REQUESTS = 5;
    private static final long TIME_WINDOW_MS = 10_000; // 10 detik

    // Tracker IP klien: IP -> [Jumlah Request, Waktu Reset]
    private final Map<String, ClientRequestInfo> ipRequestTracker = new ConcurrentHashMap<>();

    private static class ClientRequestInfo {
        int requestCount;
        long windowStartTime;

        ClientRequestInfo(long windowStartTime) {
            this.requestCount = 1;
            this.windowStartTime = windowStartTime;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Kecualikan endpoint Actuator dan Swagger dari pembatasan rate limit
        if (path.startsWith("/actuator") || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        long now = System.currentTimeMillis();

        ClientRequestInfo info = ipRequestTracker.compute(clientIp, (key, existing) -> {
            if (existing == null || (now - existing.windowStartTime) > TIME_WINDOW_MS) {
                // Buat jendela waktu 10 detik baru
                return new ClientRequestInfo(now);
            } else {
                existing.requestCount++;
                return existing;
            }
        });

        // Jika request melebihi kuota 5 request dalam 10 detik
        if (info.requestCount > MAX_REQUESTS) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // HTTP 429
            response.setContentType("application/json");
            response.getWriter().write("""
                {
                  "status": 429,
                  "error": "Too Many Requests",
                  "message": "Terlalu banyak permintaan! Maksimal 5 request per 10 detik. Silakan coba sesaat lagi."
                }
                """);
            return; // Hentikan alur, jangan lanjutkan ke Controller
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
