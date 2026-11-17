package com.belajar.config;

import com.belajar.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Matikan CSRF karena REST API menggunakan Bearer Token (Stateless)
            .csrf(csrf -> csrf.disable())

            // 2. Izinkan akses H2 Console di browser
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))

            // 3. Jangan simpan session di server (Full JWT Stateless)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 4. ATURAN IZIN AKSES PINTU MASUK API:
            .authorizeHttpRequests(auth -> auth
                // Pintu Publik (Bebas tanpa tiket)
                .requestMatchers("/api/auth/**", "/h2-console/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()

                // Pintu Khusus Penjual (Wajib punya tiket bertuliskan SELLER)
                .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("SELLER")
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("SELLER")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("SELLER")

                // Pintu Khusus Pembeli (Wajib punya tiket bertuliskan BUYER)
                .requestMatchers(HttpMethod.POST, "/api/orders/**").hasRole("BUYER")

                // Pesanan lainnya wajib login
                .requestMatchers("/api/orders/**").authenticated()

                // Semua rute lainnya wajib login
                .anyRequest().authenticated()
            )

            // 5. Pasang Satpam JWT kita sebelum satpam standar Spring Security
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}