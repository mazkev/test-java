package com.belajar.event;

import com.belajar.config.RabbitMQConfig;
import com.belajar.repository.OrderRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderInvoiceConsumer {

    private final OrderRepository orderRepository;

    public OrderInvoiceConsumer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void consumeOrderCreated(Long orderId) {
        System.out.println("\n=======================================================");
        System.out.println(">>> [RABBITMQ CONSUMER] Menerima Pesan dari Antrean: " + RabbitMQConfig.QUEUE_NAME);
        System.out.println(">>> [RABBITMQ CONSUMER] Memproses Invoice untuk Order ID #" + orderId);

        orderRepository.findById(orderId).ifPresent(order -> {
            System.out.println(">>> [RABBITMQ NOTIF] Mengirim email struk ke: " + order.getBuyer().getEmail());
            System.out.println(">>> [RABBITMQ NOTIF] Total Pembayaran: Rp " + order.getTotalAmount());
        });

        System.out.println(">>> [RABBITMQ CONSUMER SUKSES] Pesan telah berhasil diproses!");
        System.out.println("=======================================================\n");
    }
}