package com.belajar.event;

import com.belajar.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderInvoiceConsumer {

    /**
     * Listener RabbitMQ otomatis mengambil payload JSON dari antrean 'order.invoice.queue',
     * mengonversinya menjadi objek Java Record OrderMessageDto, dan mengeksekusi logika ini
     * secara asinkron di worker thread RabbitMQ.
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void consumeOrderCreated(OrderMessageDto message) {
        System.out.println("\n=======================================================");
        System.out.println(">>> [RABBITMQ CONSUMER] Menerima DTO JSON dari Antrean: " + RabbitMQConfig.QUEUE_NAME);
        System.out.println(">>> [RABBITMQ CONSUMER] Order ID    : #" + message.orderId());
        System.out.println(">>> [RABBITMQ CONSUMER] Nama Buyer  : " + message.buyerName());
        System.out.println(">>> [RABBITMQ CONSUMER] Email Buyer : " + message.buyerEmail());
        System.out.println(">>> [RABBITMQ CONSUMER] Total Bayar : Rp " + message.totalAmount());
        System.out.println(">>> [RABBITMQ CONSUMER] Waktu Order : " + message.orderDate());
        System.out.println(">>> [RABBITMQ CONSUMER] Menerbitkan struk invoice digital & kirim email...");
        System.out.println(">>> [RABBITMQ CONSUMER SUKSES] Data selesai diproses tanpa query ulang database!");
        System.out.println("=======================================================\n");
    }
}
