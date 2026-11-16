package com.belajar.model;

public enum OrderStatus {
    PENDING,    // Menunggu pembayaran
    PAID,       // Sudah dibayar
    SHIPPED,    // Barang sedang dikirim
    CANCELLED   // Pesanan dibatalkan (stok otomatis kembali ke toko)
}