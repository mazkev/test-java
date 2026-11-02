package com.belajar.service;

import com.belajar.model.Order;
import com.belajar.model.OrderStatus;
import com.belajar.repository.OrderRepository;
import com.belajar.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    // 1. Ambil seluruh order
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // 2. Logika Bisnis: Checkout Pembelian Barang
    @Transactional
    public Order checkout(Long productId, Integer quantity) {
        // Cek keberadaan produk
        var product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Produk dengan ID " + productId + " tidak ditemukan!"));

        // Cek stok
        if (product.getStock() < quantity) {
            throw new IllegalStateException("Stok tidak mencukupi! Sisa stok: " + product.getStock());
        }

        // Potong stok
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);

        // Buat Order baru
        double totalPrice = product.getPrice() * quantity;
        Order order = new Order(product.getId(), product.getName(), quantity, totalPrice);
        return orderRepository.save(order);
    }

    // 3. Logika Bisnis: Update Status & Restock Otomatis
    @Transactional
    public Order updateStatus(Long orderId, OrderStatus newStatus) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order dengan ID " + orderId + " tidak ditemukan!"));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order yang sudah CANCELLED tidak bisa diubah statusnya lagi!");
        }

        // Restock otomatis jika dibatalkan
        if (newStatus == OrderStatus.CANCELLED) {
            productRepository.findById(order.getProductId()).ifPresent(product -> {
                product.setStock(product.getStock() + order.getQuantity());
                productRepository.save(product);
            });
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }
}
