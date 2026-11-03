package com.belajar.service;

import com.belajar.model.Order;
import com.belajar.model.OrderItem;
import com.belajar.model.OrderStatus;
import com.belajar.model.Role;
import com.belajar.repository.OrderRepository;
import com.belajar.repository.ProductRepository;
import com.belajar.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // DTO untuk item di dalam keranjang belanja
    public record CartItemDto(Long productId, Integer quantity) {}

    public OrderService(OrderRepository orderRepository, 
                        ProductRepository productRepository, 
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // Checkout Keranjang Belanja Banyak Barang Sekaligus
    @Transactional
    public Order checkout(Long buyerId, List<CartItemDto> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalStateException("Keranjang belanja tidak boleh kosong!");
        }

        // 1. Validasi Pembeli
        var buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new IllegalArgumentException("Pembeli dengan ID " + buyerId + " tidak ditemukan!"));

        if (buyer.getRole() != Role.BUYER) {
            throw new IllegalStateException("Hanya akun dengan role BUYER yang bisa melakukan checkout pesanan!");
        }

        Order order = new Order(buyer);

        // 2. Loop setiap barang di keranjang belanja
        for (var item : cartItems) {
            var product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new IllegalArgumentException("Produk dengan ID " + item.productId() + " tidak ditemukan!"));

            // Validasi Stok per item
            if (product.getStock() < item.quantity()) {
                throw new IllegalStateException("Stok tidak mencukupi untuk produk: " + product.getName() 
                        + " (Sisa stok: " + product.getStock() + ", Diminta: " + item.quantity() + ")");
            }

            // Potong stok produk
            product.setStock(product.getStock() - item.quantity());
            productRepository.save(product);

            // Buat OrderItem dan tambahkan ke Order (subtotal otomatis dihitung)
            OrderItem orderItem = new OrderItem(order, product.getId(), product.getName(), product.getPrice(), item.quantity());
            order.addItem(orderItem);
        }

        // Simpan Order (CascadeType.ALL akan otomatis menyimpan seluruh OrderItem ke tabel order_items)
        return orderRepository.save(order);
    }

    // Update Status & Restock Otomatis jika Cancelled
    @Transactional
    public Order updateStatus(Long orderId, OrderStatus newStatus) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order dengan ID " + orderId + " tidak ditemukan!"));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order yang sudah CANCELLED tidak bisa diubah statusnya lagi!");
        }

        // Jika dibatalkan, kembalikan stok SEMUA barang yang ada di dalam order tersebut!
        if (newStatus == OrderStatus.CANCELLED) {
            for (var item : order.getItems()) {
                productRepository.findById(item.getProductId()).ifPresent(product -> {
                    product.setStock(product.getStock() + item.getQuantity());
                    productRepository.save(product);
                });
            }
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }
}
