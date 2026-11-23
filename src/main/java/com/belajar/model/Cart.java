package com.belajar.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1 Keranjang dimiliki secara khusus oleh 1 Pembeli
    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    // 1 Keranjang memuat daftar banyak CartItem
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    public Cart() {}

    public Cart(User user) {
        this.user = user;
    }

    // Helper: Menghitung total seluruh belanjaan di dalam keranjang
    public Double getTotalPrice() {
        return items.stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
    }

    // Helper: Menghitung total jumlah fisik barang di keranjang
    public Integer getTotalItems() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    // Getter dan Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }
}