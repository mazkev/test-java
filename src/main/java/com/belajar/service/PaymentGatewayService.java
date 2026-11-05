package com.belajar.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

@Service
public class PaymentGatewayService {

    public record PaymentResult(boolean success, String transactionId, String statusMessage) {}

    /**
     * Fase 17: Circuit Breaker Pattern menggunakan Resilience4j.
     * Jika terjadi kegagalan berturut-turut melebihi ambang batas (failureRateThreshold = 50%),
     * sirkuit akan otomatis OPEN (terbuka) dan langsung mengalihkan eksekusi ke fallbackPayment
     * tanpa membebani sistem vendor eksternal yang sedang down.
     */
    @CircuitBreaker(name = "paymentService", fallbackMethod = "fallbackPayment")
    public PaymentResult processPayment(Long orderId, Double amount, boolean simulateFailure) {
        if (simulateFailure) {
            System.err.println(">>> [PAYMENT VENDOR ERROR] Koneksi ke payment gateway timeout untuk Order #" + orderId);
            throw new RuntimeException("Koneksi ke Payment Gateway vendor gagal / timeout!");
        }

        System.out.println(">>> [PAYMENT SUCCESS] Transaksi Order #" + orderId + " sebesar Rp " + amount + " sukses.");
        return new PaymentResult(true, "TRX-ORD-" + orderId + "-" + System.currentTimeMillis(), "Pembayaran Berhasil");
    }

    /**
     * Fallback Method: Otomatis dipanggil saat terjadi Exception atau saat sirkuit dalam kondisi OPEN.
     */
    public PaymentResult fallbackPayment(Long orderId, Double amount, boolean simulateFailure, Throwable throwable) {
        System.err.println(">>> [CIRCUIT BREAKER FALLBACK] Mengaktifkan mekanisme cadangan untuk Order #" + orderId);
        System.err.println(">>> [CIRCUIT BREAKER SEBAB] " + throwable.getMessage());
        return new PaymentResult(
                false, 
                "FALLBACK-TRX-" + orderId, 
                "Sistem Pembayaran Eksternal sedang mengalami gangguan (" + throwable.getClass().getSimpleName() 
                + "). Transaksi dialihkan ke metode Manual Transfer."
        );
    }
}

