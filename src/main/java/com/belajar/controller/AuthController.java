package com.belajar.controller;

import com.belajar.model.Role;
import com.belajar.model.User;
import com.belajar.repository.UserRepository;
import com.belajar.security.JwtUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 1. DTO Request Register
    public record RegisterRequest(
        @NotBlank(message = "Nama tidak boleh kosong!") String name,
        @NotBlank(message = "Email tidak boleh kosong!") @Email(message = "Format email tidak valid!") String email,
        @NotBlank(message = "Password tidak boleh kosong!") String password,
        @NotNull(message = "Role wajib diisi (BUYER atau SELLER)!") Role role
    ) {}

    // 2. DTO Request Login
    public record LoginRequest(
        @NotBlank(message = "Email tidak boleh kosong!") String email,
        @NotBlank(message = "Password tidak boleh kosong!") String password
    ) {}

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // 1. [POST] Registrasi Pengguna Baru (Password diacak dengan BCrypt)
    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        // Cek email duplikat
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalStateException("Email " + request.email() + " sudah terdaftar!");
        }

        // ENKRIPSI PASSWORD: Kata sandi diacak sebelum disimpan ke database
        String encodedPassword = passwordEncoder.encode(request.password());

        User user = new User(request.name(), request.email(), encodedPassword, request.role());
        User savedUser = userRepository.save(user);

        return new ResponseEntity<>(Map.of(
            "message", "Registrasi berhasil!",
            "userId", savedUser.getId(),
            "name", savedUser.getName(),
            "email", savedUser.getEmail(),
            "role", savedUser.getRole()
        ), HttpStatus.CREATED);
    }

    // 2. [POST] Login Pengguna (Menghasilkan Token JWT)
    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        // Cari user berdasarkan email
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Email atau password salah!"));

        // Verifikasi apakah password cocok dengan hash BCrypt di database
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Email atau password salah!");
        }

        // Generate Token JWT jika password cocok
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());

        return ResponseEntity.ok(Map.of(
            "message", "Login berhasil!",
            "token", token,
            "tokenType", "Bearer",
            "userId", user.getId(),
            "role", user.getRole()
        ));
    }
}
