package com.belajar.event;

import com.belajar.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderInvoiceConsumer {

    /**
     * Consumer Utama: Membaca antrean 'order.invoice.queue'.
     * Jika terjadi kegagalan (Exception), Spring AMQP otomatis melakukan Retry 3x
     * dengan exponential backoff. Jika tetap gagal, pesan dialihkan ke Dead Letter Queue (DLQ).
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void consumeOrderCreated(OrderMessageDto message) {
        System.out.println("\n=======================================================");
        System.out.println(">>> [RABBITMQ WORKER] Memproses Order #" + message.orderId() + " (" + message.buyerEmail() + ")");

        // Simulasi kegagalan untuk menguji ketangguhan Retry & DLQ
        if (message.buyerEmail() != null && message.buyerEmail().contains("fail")) {
            System.err.println(">>> [SIMULASI ERROR] Server invoice vendor timeout untuk email: " + message.buyerEmail());
            throw new RuntimeException("Simulasi Gangguan Vendor Invoice Eksternal!");
        }

        System.out.println(">>> [RABBITMQ CONSUMER] Order ID    : #" + message.orderId());
        System.out.println(">>> [RABBITMQ CONSUMER] Nama Buyer  : " + message.buyerName());
        System.out.println(">>> [RABBITMQ CONSUMER] Total Bayar : Rp " + message.totalAmount());
        System.out.println(">>> [RABBITMQ CONSUMER SUKSES] Email invoice berhasil dikirim!");
        System.out.println("=======================================================\n");
    }

    /**
     * Fase 15: Dead Letter Consumer
     * Menangani pesan yang gagal diproses setelah 3x retry agar tidak ada transaksi yang hilang (Zero Data Loss).
     */
    @RabbitListener(queues = RabbitMQConfig.DLQ_QUEUE_NAME)
    public void consumeDeadLetterOrder(OrderMessageDto deadMessage) {
        System.err.println("\n🚨 =======================================================");
        System.err.println("🚨 [DEAD LETTER QUEUE ALERT] Terdeteksi Pesan Gagal Diproses!");
        System.err.println("🚨 [DLQ INVENTORY] Order ID: #" + deadMessage.orderId());
        System.err.println("🚨 [DLQ INVENTORY] Buyer   : " + deadMessage.buyerEmail());
        System.err.println("🚨 [DLQ ACTION] Pesan telah diisolasi di antrean: " + RabbitMQConfig.DLQ_QUEUE_NAME);
        System.err.println("🚨 [DLQ ACTION] Siap di-replay / diinvestigasi oleh tim engineering.");
        System.err.println("🚨 =======================================================\n");
    }
}
