package com.belajar.event;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO pesan event yang dikirim ke RabbitMQ.
 * Menerapkan pola Event-Carried State Transfer (ECST) agar consumer
 * tidak perlu query ulang ke database.
 */
public record OrderMessageDto(
    Long orderId,
    String buyerName,
    String buyerEmail,
    Double totalAmount,
    LocalDateTime orderDate
) implements Serializable {
}

