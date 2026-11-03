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
    String category,

    @NotNull(message = "Seller ID wajib diisi!")
    Long sellerId
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

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Upload produk baru milik Toko tertentu
    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody CreateProductRequest request) {
        var seller = userRepository.findById(request.sellerId())
                .orElseThrow(() -> new IllegalArgumentException("Seller dengan ID " + request.sellerId() + " tidak ditemukan!"));

        // Validasi: Pembeli biasa tidak boleh upload produk
        if (seller.getRole() != Role.SELLER) {
            throw new IllegalStateException("Hanya akun dengan role SELLER yang bisa menjual produk!");
        }

        Product product = new Product(
            request.name(), 
            request.description(), 
            request.price(), 
            request.stock(), 
            request.category()
        );
        product.setSeller(seller);

        return new ResponseEntity<>(productRepository.save(product), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
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
