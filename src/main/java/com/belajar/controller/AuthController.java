package com.belajar.controller;

import com.belajar.dto.AuthResponse;
import com.belajar.dto.LoginRequest;
import com.belajar.model.User;
import com.belajar.repository.UserRepository;
import com.belajar.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // 1. Cari user berdasarkan email
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Email atau password salah!"));

        // 2. Cocokkan password mentah dengan hash di database menggunakan BCrypt
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Email atau password salah!");
        }

        // 3. Jika cocok, cetak tiket gelang JWT!
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return ResponseEntity.ok(new AuthResponse(token, "Bearer", user.getId(), user.getEmail(), user.getRole().name()));
    }
}