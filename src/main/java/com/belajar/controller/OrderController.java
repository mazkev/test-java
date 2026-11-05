package com.belajar.controller;

import com.belajar.model.Order;
import com.belajar.model.OrderStatus;
import com.belajar.service.OrderService;
import com.belajar.service.PaymentGatewayService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final PaymentGatewayService paymentGatewayService;

    public record CheckoutRequest(List<OrderService.CartItemDto> items) {}
    public record UpdateStatusRequest(OrderStatus status) {}

    public OrderController(OrderService orderService, PaymentGatewayService paymentGatewayService) {
        this.orderService = orderService;
        this.paymentGatewayService = paymentGatewayService;
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

    /**
     * Fase 17: Uji Coba Circuit Breaker
     */
    @PostMapping("/{id}/pay")
    public ResponseEntity<PaymentGatewayService.PaymentResult> payOrder(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean simulateFailure) {
        PaymentGatewayService.PaymentResult result = paymentGatewayService.processPayment(id, 170000.0, simulateFailure);
        return ResponseEntity.ok(result);
    }
}
