package com.belajar.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentGatewayServiceTest {

    private PaymentGatewayService paymentGatewayService;

    @BeforeEach
    void setUp() {
        paymentGatewayService = new PaymentGatewayService();
    }

    @Test
    @DisplayName("Skenario 1: Transaksi Normal - Mengembalikan ID Transaksi Resmi")
    void processPayment_Success() {
        // WHEN: Pembayaran dipanggil dengan simulateFailure = false
        var result = paymentGatewayService.processPayment(1L, 100000.0, false);

        // THEN: Harus berhasil dan menerbitkan TRX ID
        assertThat(result.success()).isTrue();
        assertThat(result.transactionId()).startsWith("TRX-ORD-1-");
        assertThat(result.statusMessage()).isEqualTo("Pembayaran Berhasil");
    }

    @Test
    @DisplayName("Skenario 2: Transaksi Vendor Down - Fallback Aktif Mengalihkan ke Manual Transfer")
    void fallbackPayment_GracefulMitigation() {
        // GIVEN: Terjadi Exception jaringan saat menghubungi vendor pembayaran
        var exception = new RuntimeException("Simulasi Jaringan Terputus");

        // WHEN: Fallback method dieksekusi
        var result = paymentGatewayService.fallbackPayment(1L, 100000.0, true, exception);

        // THEN: Tidak ada crash, transaksi dialihkan secara aman
        assertThat(result.success()).isFalse();
        assertThat(result.transactionId()).isEqualTo("FALLBACK-TRX-1");
        assertThat(result.statusMessage()).contains("Manual Transfer");
    }
}