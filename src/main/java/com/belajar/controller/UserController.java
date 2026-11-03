package com.belajar.controller;

import com.belajar.model.Product;
import com.belajar.model.Role;
import com.belajar.model.User;
import com.belajar.repository.ProductRepository;
import com.belajar.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public record CreateUserRequest(String name, String email, Role role) {}

    public UserController(UserRepository userRepository, ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    // 1. [GET] Ambil semua user (bisa filter ?role=SELLER atau ?role=BUYER)
    @GetMapping
    public List<User> getAllUsers(@RequestParam(required = false) Role role) {
        if (role != null) {
            return userRepository.findByRole(role);
        }
        return userRepository.findAll();
    }

    // 2. [POST] Registrasi Pengguna Baru (Buka Toko atau Buat Akun Pembeli)
    @PostMapping
    public ResponseEntity<User> registerUser(@RequestBody CreateUserRequest request) {
        // Cek apakah email sudah terdaftar
        userRepository.findByEmail(request.email()).ifPresent(u -> {
            throw new IllegalStateException("Email " + request.email() + " sudah terdaftar!");
        });

        User user = new User(request.name(), request.email(), request.role());
        return new ResponseEntity<>(userRepository.save(user), HttpStatus.CREATED);
    }

    // 3. [GET] Melihat etalase toko (Semua produk yang dijual oleh toko tertentu)
    // GET /api/users/1/products
    @GetMapping("/{id}/products")
    public List<Product> getProductsBySeller(@PathVariable Long id) {
        // Cek apakah usernya ada
        userRepository.findById(id).orElseThrow(() -> 
            new IllegalArgumentException("User dengan ID " + id + " tidak ditemukan!"));

        return productRepository.findBySellerId(id);
    }
}
