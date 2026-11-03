package com.belajar.controller;

import com.belajar.model.Order;
import com.belajar.model.OrderStatus;
import com.belajar.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public record CheckoutRequest(List<OrderService.CartItemDto> items) {}
    public record UpdateStatusRequest(OrderStatus status) {}

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PostMapping
    public ResponseEntity<Order> checkout(
            @RequestBody CheckoutRequest request,
            Authentication authentication) {
        // Ambil email pembeli dari Token JWT yang login
        String buyerEmail = authentication.getName();
        Order order = orderService.checkout(buyerEmail, request.items());
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest request) {
        Order order = orderService.updateStatus(id, request.status());
        return ResponseEntity.ok(order);
    }
}
