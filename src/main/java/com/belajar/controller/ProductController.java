package com.belajar.controller;

import com.belajar.model.Product;
import com.belajar.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepository;

    // Dependency Injection: Spring otomatis menyuntikkan ProductRepository ke sini
    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // 1. [GET] Semua Produk -> otomatis query: SELECT * FROM products
    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // 2. [GET] Detail Produk by ID -> query: SELECT * FROM products WHERE id = ?
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. [POST] Tambah Produk Baru -> query: INSERT INTO products ...
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product saved = productRepository.save(product);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // 4. [PUT] Update Produk -> query: UPDATE products SET ... WHERE id = ?
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product request) {
        return productRepository.findById(id).map(existing -> {
            existing.setName(request.getName());
            existing.setDescription(request.getDescription());
            existing.setPrice(request.getPrice());
            existing.setStock(request.getStock());
            existing.setCategory(request.getCategory());
            Product updated = productRepository.save(existing);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 5. [DELETE] Hapus Produk -> query: DELETE FROM products WHERE id = ?
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
