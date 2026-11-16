package com.belajar.controller;

import com.belajar.dto.CheckoutRequest;
import com.belajar.model.Order;
import com.belajar.model.OrderStatus;
import com.belajar.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 1. POST /api/orders : Checkout Keranjang Belanja
    @PostMapping
    public ResponseEntity<Order> checkout(@Valid @RequestBody CheckoutRequest request) {
        Order createdOrder = orderService.checkout(request);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    // 2. PUT /api/orders/{id}/status : Ubah Status (PAID / CANCELLED)
    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        Order updated = orderService.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }
}