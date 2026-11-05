package com.belajar.controller;

import com.belajar.model.Product;
import com.belajar.model.Role;
import com.belajar.repository.ProductRepository;
import com.belajar.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public record CreateProductRequest(
    @NotBlank(message = "Nama produk tidak boleh kosong!")
    String name,

    String description,

    @NotNull(message = "Harga produk wajib diisi!")
    @Positive(message = "Harga produk harus lebih besar dari 0!")
    Double price,

    @NotNull(message = "Stok produk wajib diisi!")
    @Min(value = 0, message = "Stok produk tidak boleh bernilai negatif!")
    Integer stock,

    @NotBlank(message = "Kategori produk tidak boleh kosong!")
    String category
    ) {}

    public ProductController(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public org.springframework.data.domain.Page<Product> getAllProducts(
            org.springframework.data.domain.Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    // Mengambil detail produk: otomatis disimpan ke Redis Distributed Cache
    @GetMapping("/{id}")
    @Cacheable(value = "products", key = "#id")
    public Product getProductById(@PathVariable Long id) {
        System.out.println(">>> [LOG QUERY SQL] Mengambil produk ID " + id + " dari Database SQL...");
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produk dengan ID " + id + " tidak ditemukan!"));
    }

    // Upload produk baru milik Toko tertentu
    @PostMapping
    public ResponseEntity<Product> createProduct(
        @Valid @RequestBody CreateProductRequest request,
        org.springframework.security.core.Authentication authentication) {
    
    // Ambil email toko yang sedang login dari Token JWT
    String sellerEmail = authentication.getName();
    var seller = userRepository.findByEmail(sellerEmail)
            .orElseThrow(() -> new IllegalArgumentException("Toko tidak ditemukan!"));

    Product product = new Product(
        request.name(), 
        request.description(), 
        request.price(), 
        request.stock(), 
        request.category()
    );
    product.setSeller(seller); // Otomatis mengikat ke toko yang memegang token!

    return new ResponseEntity<>(productRepository.save(product), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @CacheEvict(value = "products", key = "#id")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product request) {
        return productRepository.findById(id).map(existing -> {
            existing.setName(request.getName());
            existing.setDescription(request.getDescription());
            existing.setPrice(request.getPrice());
            existing.setStock(request.getStock());
            existing.setCategory(request.getCategory());
            return ResponseEntity.ok(productRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = "products", key = "#id")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    public org.springframework.data.domain.Page<Product> searchByName(
            @RequestParam String name, 
            org.springframework.data.domain.Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @GetMapping("/category/{category}")
    public org.springframework.data.domain.Page<Product> getByCategory(
            @PathVariable String category, 
            org.springframework.data.domain.Pageable pageable) {
        return productRepository.findByCategory(category, pageable);
    }
}
