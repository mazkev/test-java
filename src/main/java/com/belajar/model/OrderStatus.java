package com.belajar.model;

public enum OrderStatus {
    PENDING,    // Pesanan baru dibuat, menunggu pembayaran
    PAID,       // Pembayaran sudah dikonfirmasi
    SHIPPED,    // Barang sedang dikirim kurir
    DELIVERED,  // Barang sudah sampai di tangan pembeli
    CANCELLED   // Pesanan dibatalkan
}
