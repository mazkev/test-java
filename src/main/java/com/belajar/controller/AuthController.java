package com.belajar.controller;

import com.belajar.dto.AuthResponse;
import com.belajar.dto.LoginRequest;
import com.belajar.dto.RegisterRequest;
import com.belajar.model.Role;
import com.belajar.model.User;
import com.belajar.repository.UserRepository;
import com.belajar.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    // 1. Endpoint Registrasi Pengguna Baru
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Cek apakah email sudah terdaftar sebelumnya
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalStateException("Email " + request.email() + " sudah terdaftar!");
        }

        // Hash password menggunakan BCrypt
        String hashedPassword = passwordEncoder.encode(request.password());

        // Default role: jika tidak ditentukan, jadikan BUYER
        Role role = request.role() != null ? request.role() : Role.BUYER;

        // Simpan user baru ke database
        User newUser = new User(
                request.name(),
                request.email(),
                hashedPassword,
                role,
                request.phoneNumber(),
                request.city(),
                request.address()
        );
        User savedUser = userRepository.save(newUser);

        // Cetak token JWT agar user langsung login
        String token = jwtService.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name());

        return new ResponseEntity<>(
                new AuthResponse(token, "Bearer", savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name()),
                HttpStatus.CREATED
        );
    }

    // 2. Endpoint Login Pengguna Lama
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Email atau password salah!"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Email atau password salah!");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return ResponseEntity.ok(new AuthResponse(token, "Bearer", user.getId(), user.getEmail(), user.getRole().name()));
    }
}