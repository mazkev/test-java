package com.belajar.event;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class OrderNotificationListener {

    /**
     * @Async menjalankan method ini di background thread (Virtual Thread Java 21),
     * sehingga endpoint checkout langsung merespon tanpa menunggu proses ini selesai!
     */
    @Async
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        var order = event.order();
        String currentThread = Thread.currentThread().toString();

        System.out.println("\n=======================================================");
        System.out.println(">>> [ASYNC WORKER] Memulai Pengiriman Invoice Order #" + order.getId());
        System.out.println(">>> [THREAD INFO] Dijalankan di thread: " + currentThread);
        System.out.println(">>> [NOTIF EMAIL] Mengirim email struk ke: " + order.getBuyer().getEmail());
        System.out.println(">>> [NOTIF EMAIL] Total Tagihan: Rp " + order.getTotalAmount());

        // Simulasi proses kirim email ke SMTP pihak ketiga (delay 2 detik)
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println(">>> [ASYNC WORKER SUKSES] Email invoice Order #" + order.getId() + " terkirim!");
        System.out.println("=======================================================\n");
    }
}
