package com.belajar.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    // 1. Model data Produk Marketplace (pakai record agar ringkas)
    public record Product(
        Long id, 
        String name, 
        String description, 
        Double price, 
        Integer stock, 
        String category
    ) {}

    // Simpan data produk di memori sementara
    private final List<Product> products = new ArrayList<>();
    private long currentId = 1;

    public ProductController() {
        // Sample data awal barang di marketplace
        products.add(new Product(currentId++, "Kopi Gayo Arabika 250g", "Biji kopi sangrai premium dari Aceh", 85000.0, 50, "Minuman"));
        products.add(new Product(currentId++, "Mechanical Keyboard RGB", "Switch Biru, Layout 75% tactile", 450000.0, 20, "Elektronik"));
    }

    // [GET] Ambil semua produk katalog
    // Contoh: GET http://localhost:8080/api/products
    @GetMapping
    public List<Product> getAllProducts() {
        return products;
    }

    // [GET] Ambil detail 1 produk berdasarkan ID
    // Contoh: GET http://localhost:8080/api/products/1
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        for (var p : products) {
            if (p.id().equals(id)) {
                return ResponseEntity.ok(p); // Status 200 OK
            }
        }
        return ResponseEntity.notFound().build(); // Status 404 Not Found
    }

    // [POST] Tambah produk baru (Jual barang)
    // Contoh: POST http://localhost:8080/api/products
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product request) {
        // Buat objek produk baru dengan ID auto-increment
        var newProduct = new Product(
            currentId++, 
            request.name(), 
            request.description(), 
            request.price(), 
            request.stock(), 
            request.category()
        );
        products.add(newProduct);
        return new ResponseEntity<>(newProduct, HttpStatus.CREATED); // Status 201 Created
    }

    // [PUT] Update data produk (misal ganti harga atau update stok)
    // Contoh: PUT http://localhost:8080/api/products/1
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product request) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).id().equals(id)) {
                var updated = new Product(
                    id, 
                    request.name(), 
                    request.description(), 
                    request.price(), 
                    request.stock(), 
                    request.category()
                );
                products.set(i, updated); // Timpa data lama dengan data baru
                return ResponseEntity.ok(updated);
            }
        }
        return ResponseEntity.notFound().build();
    }

    // [DELETE] Hapus produk dari marketplace
    // Contoh: DELETE http://localhost:8080/api/products/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        boolean removed = products.removeIf(p -> p.id().equals(id));
        if (removed) {
            return ResponseEntity.noContent().build(); // Status 204 No Content
        }
        return ResponseEntity.notFound().build();
    }
}
