package com.belajar.service;

import com.belajar.dto.CheckoutRequest;
import com.belajar.model.*;
import com.belajar.repository.CouponRepository;
import com.belajar.repository.OrderRepository;
import com.belajar.repository.ProductRepository;
import com.belajar.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository; // 1. Suntikkan CouponRepository

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, UserRepository userRepository, CouponRepository couponRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.couponRepository = couponRepository;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Transactional
    public Order checkout(CheckoutRequest request, String buyerEmail) {
        User buyer = userRepository.findByEmail(buyerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Pembeli dengan email " + buyerEmail + " tidak ditemukan!"));

        if (buyer.getRole() != Role.BUYER) {
            throw new IllegalStateException("Hanya akun dengan role BUYER yang berhak belanja!");
        }

        String address = (request.shippingAddress() != null && !request.shippingAddress().isBlank()) 
                ? request.shippingAddress() 
                : buyer.getAddress();

        String courier = request.shippingCourier() != null ? request.shippingCourier() : "JNE Reguler";
        Double shippingCost = request.shippingCost() != null ? request.shippingCost() : 15000.0;
        String paymentMethod = request.paymentMethod() != null ? request.paymentMethod() : "BCA_VA";

        Order order = new Order(buyer, address, courier, shippingCost, paymentMethod);

        // Loop setiap barang belanjaan
        for (var item : request.items()) {
            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new IllegalArgumentException("Produk ID " + item.productId() + " tidak ditemukan!"));

            if (product.getStock() < item.quantity()) {
                throw new IllegalStateException("Stok tidak mencukupi untuk: " + product.getName() 
                        + " (Sisa: " + product.getStock() + ", Diminta: " + item.quantity() + ")");
            }

            product.setStock(product.getStock() - item.quantity());
            product.setTotalSold(product.getTotalSold() + item.quantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem(order, product.getId(), product.getName(), product.getPrice(), item.quantity());
            order.addItem(orderItem);
        }

        // 2. LOGIKA KUPON DISKON PROMO
        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            Coupon coupon = couponRepository.findByCodeIgnoreCase(request.couponCode().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Kupon dengan kode '" + request.couponCode() + "' tidak ditemukan!"));

            if (!coupon.getActive()) {
                throw new IllegalStateException("Kupon '" + coupon.getCode() + "' sudah tidak aktif!");
            }
            if (coupon.getExpiryDate() != null && LocalDateTime.now().isAfter(coupon.getExpiryDate())) {
                throw new IllegalStateException("Kupon '" + coupon.getCode() + "' sudah kedaluwarsa!");
            }
            if (coupon.getUsedCount() >= coupon.getUsageLimit()) {
                throw new IllegalStateException("Kupon '" + coupon.getCode() + "' sudah habis kuota pemakaiannya!");
            }
            if (order.getTotalAmount() < coupon.getMinPurchase()) {
                throw new IllegalStateException("Total belanja belum memenuhi syarat minimal belanja kupon (Min: Rp " + coupon.getMinPurchase() + ")!");
            }

            Double discount = coupon.calculateDiscount(order.getTotalAmount());
            order.setCouponCode(coupon.getCode());
            order.setDiscountAmount(discount);

            // Hitung ulang Grand Total: (Total Barang - Diskon) + Ongkir
            double finalGrandTotal = Math.max(0.0, (order.getTotalAmount() - discount) + order.getShippingCost());
            order.setGrandTotal(finalGrandTotal);

            // Tambah counter pemakaian kupon
            coupon.setUsedCount(coupon.getUsedCount() + 1);
            couponRepository.save(coupon);
        }

        return orderRepository.save(order);
    }

    @Transactional
    public Order updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Pesanan ID " + orderId + " tidak ditemukan!"));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Pesanan yang sudah CANCELLED tidak bisa diubah statusnya lagi!");
        }

        // Jika pesanan dibatalkan, kembalikan stok & kuota kupon!
        if (newStatus == OrderStatus.CANCELLED) {
            for (var item : order.getItems()) {
                productRepository.findById(item.getProductId()).ifPresent(product -> {
                    product.setStock(product.getStock() + item.getQuantity());
                    productRepository.save(product);
                });
            }

            // Kembalikan kuota kupon (refund quota)
            if (order.getCouponCode() != null) {
                couponRepository.findByCodeIgnoreCase(order.getCouponCode()).ifPresent(coupon -> {
                    coupon.setUsedCount(Math.max(0, coupon.getUsedCount() - 1));
                    couponRepository.save(coupon);
                });
            }
        }

        if (newStatus == OrderStatus.SHIPPED && order.getTrackingNumber() == null) {
            order.setTrackingNumber("RESI-JNE-" + System.currentTimeMillis());
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }
}