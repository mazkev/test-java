package com.belajar.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Ambil Header Authorization
        String authHeader = request.getHeader("Authorization");

        // 2. Cek apakah ada Bearer Token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Ambil string token setelah "Bearer "

            // 3. Validasi keaslian Token
            if (jwtUtil.isTokenValid(token)) {
                var claims = jwtUtil.extractClaims(token);
                String email = claims.getSubject();
                String role = claims.get("role", String.class);

                // 4. Daftarkan user ke sesi keamanan Spring (SecurityContext)
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                var authentication = new UsernamePasswordAuthenticationToken(email, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // Lanjutkan request ke Controller
        filterChain.doFilter(request, response);
    }
}
