package com.belajar.event;

import com.belajar.model.Order;

/**
 * Event immutable yang membawa data Order yang baru saja selesai disimpan.
 */
public record OrderCreatedEvent(Order order) {
}
