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
    @DisplayName("Payment Gateway Normal: Mengembalikan ID Transaksi Resmi")
    void processPayment_Success() {
        var result = paymentGatewayService.processPayment(1L, 100000.0, false);

        assertThat(result.success()).isTrue();
        assertThat(result.transactionId()).startsWith("TRX-ORD-1-");
        assertThat(result.statusMessage()).isEqualTo("Pembayaran Berhasil");
    }

    @Test
    @DisplayName("Payment Gateway Fallback: Mengalihkan transaksi saat vendor mengalami kegagalan")
    void fallbackPayment_GracefulMitigation() {
        var exception = new RuntimeException("Simulasi Jaringan Terputus");
        var result = paymentGatewayService.fallbackPayment(1L, 100000.0, true, exception);

        assertThat(result.success()).isFalse();
        assertThat(result.transactionId()).isEqualTo("FALLBACK-TRX-1");
        assertThat(result.statusMessage()).contains("Manual Transfer");
    }
}

