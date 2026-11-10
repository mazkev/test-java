package com.belajar;

import jakarta.persistence.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SpringBootApplication
public class MyApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyApiApplication.class, args);
    }

    // CommandLineRunner: Otomatis mengisi data awal ke Database SQL saat server baru menyala!
    @Bean
    public CommandLineRunner initData(ProductRepository repository) {
        return args -> {
            repository.save(new Product("Kopi Gayo Arabika", 85000.0, 20));
            repository.save(new Product("Teh Hitam Walini", 25000.0, 50));
        };
    }
}

// ========================================================
// 1. MODEL / ENTITY (Mewakili Tabel SQL "products")
// ========================================================
@Entity
@Table(name = "products")
class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto Increment SQL
    private Long id;

    private String name;
    private Double price;
    private Integer stock;

    public Product() {} // Wajib ada untuk Hibernate

    public Product(String name, Double price, Integer stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    // Getter dan Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}

// ========================================================
// 2. REPOSITORY (Penyedia Perintah SQL Otomatis)
// ========================================================
@Repository
interface ProductRepository extends JpaRepository<Product, Long> {
    // Kosong pun sudah punya: findAll(), findById(), save(), deleteById()!
}

// ========================================================
// 3. CONTROLLER (Resepsionis Web Terhubung ke Database)
// ========================================================
@RestController
@RequestMapping("/api/products")
class ProductController {

    private final ProductRepository productRepository;

    // Spring otomatis menyuntikkan gudang database (ProductRepository) ke Kasir
    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // 1. READ ALL (Otomatis: SELECT * FROM products)
    @GetMapping
    public List<Product> getAll() {
        return productRepository.findAll();
    }

    // 2. READ BY ID (Otomatis: SELECT * FROM products WHERE id = ?)
    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(product -> ResponseEntity.ok(product))
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. CREATE (Otomatis: INSERT INTO products ...)
    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product) {
        Product saved = productRepository.save(product);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // 4. UPDATE (Otomatis: UPDATE products SET ...)
    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable Long id, @RequestBody Product product) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        product.setId(id);
        Product updated = productRepository.save(product);
        return ResponseEntity.ok(updated);
    }

    // 5. DELETE (Otomatis: DELETE FROM products WHERE id = ?)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}