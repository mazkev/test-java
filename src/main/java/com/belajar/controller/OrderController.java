package com.belajar.controller;

import com.belajar.model.Order;
import com.belajar.repository.OrderRepository;
import com.belajar.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    // DTO (Data Transfer Object) ringkas untuk menerima request checkout dari client
    public record CheckoutRequest(Long productId, Integer quantity) {}

    public OrderController(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    // 1. [GET] Melihat seluruh riwayat transaksi pesanan
    // GET http://localhost:8080/api/orders
    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // 2. [POST] Checkout Pemesanan Barang (dengan Validasi & Potong Stok)
    // POST http://localhost:8080/api/orders
    @PostMapping
    @Transactional // Menjamin transaksi ACID (jika gagal di tengah jalan, rollback otomatis)
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequest request) {

        // Validasi 1: Cari apakah barangnya ada
        var productOpt = productRepository.findById(request.productId());
        if (productOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Produk dengan ID " + request.productId() + " tidak ditemukan!"));
        }

        var product = productOpt.get();

        // Validasi 2: Cek ketersediaan stok barang
        if (product.getStock() < request.quantity()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "error", "Stok tidak mencukupi!",
                        "sisa_stok", product.getStock(),
                        "diminta", request.quantity()
                    ));
        }

        // Langkah A: Potong stok barang di tabel products
        product.setStock(product.getStock() - request.quantity());
        productRepository.save(product);

        // Langkah B: Hitung total harga & simpan transaksi ke tabel orders
        double totalPrice = product.getPrice() * request.quantity();
        Order order = new Order(product.getId(), product.getName(), request.quantity(), totalPrice);
        Order savedOrder = orderRepository.save(order);

        return new ResponseEntity<>(savedOrder, HttpStatus.CREATED);
    }
}
