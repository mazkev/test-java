package com.belajar.service;

import com.belajar.dto.CheckoutRequest;
import com.belajar.model.*;
import com.belajar.repository.OrderRepository;
import com.belajar.repository.ProductRepository;
import com.belajar.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Transactional
    public Order checkout(CheckoutRequest request, String buyerEmail) {
        // 1. Ambil data pembeli langsung dari email yang ada di tiket JWT
        User buyer = userRepository.findByEmail(buyerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Pembeli dengan email " + buyerEmail + " tidak ditemukan!"));

        if (buyer.getRole() != Role.BUYER) {
            throw new IllegalStateException("Hanya akun dengan role BUYER yang berhak belanja!");
        }

        // Jika alamat tidak diisi di request, pakai alamat tersimpan di profil pembeli
        String address = (request.shippingAddress() != null && !request.shippingAddress().isBlank()) 
                ? request.shippingAddress() 
                : buyer.getAddress();

        String courier = request.shippingCourier() != null ? request.shippingCourier() : "JNE Reguler";
        Double shippingCost = request.shippingCost() != null ? request.shippingCost() : 15000.0;
        String paymentMethod = request.paymentMethod() != null ? request.paymentMethod() : "BCA_VA";

        Order order = new Order(buyer, address, courier, shippingCost, paymentMethod);

        // 2. Loop setiap barang di keranjang belanja
        for (var item : request.items()) {
            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new IllegalArgumentException("Produk ID " + item.productId() + " tidak ditemukan!"));

            // Validasi Stok
            if (product.getStock() < item.quantity()) {
                throw new IllegalStateException("Stok tidak mencukupi untuk: " + product.getName() 
                        + " (Sisa: " + product.getStock() + ", Diminta: " + item.quantity() + ")");
            }

            // Potong stok dan tambah angka total terjual!
            product.setStock(product.getStock() - item.quantity());
            product.setTotalSold(product.getTotalSold() + item.quantity());
            productRepository.save(product);

            // Buat snapshot rincian barang
            OrderItem orderItem = new OrderItem(order, product.getId(), product.getName(), product.getPrice(), item.quantity());
            order.addItem(orderItem);
        }

        // 3. Simpan pesanan utuh ke database SQL
        return orderRepository.save(order);
    }

    // Fitur Ubah Status & Auto-Restock saat pesanan dibatalkan
    @Transactional
    public Order updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Pesanan ID " + orderId + " tidak ditemukan!"));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Pesanan yang sudah CANCELLED tidak bisa diubah statusnya lagi!");
        }

        // FITUR AUTO-RESTOCK: Jika pesanan dibatalkan, kembalikan stok barang ke toko!
        if (newStatus == OrderStatus.CANCELLED) {
            for (var item : order.getItems()) {
                productRepository.findById(item.getProductId()).ifPresent(product -> {
                    product.setStock(product.getStock() + item.getQuantity()); // Kembalikan stok!
                    productRepository.save(product);
                });
            }
        }

            // Jika status berubah jadi SHIPPED, generate nomor resi otomatis (kalau belum ada resi)
        if (newStatus == OrderStatus.SHIPPED && order.getTrackingNumber() == null) {
            order.setTrackingNumber("RESI-JNE-" + System.currentTimeMillis());
        }


        order.setStatus(newStatus);
        return orderRepository.save(order);
    }
}