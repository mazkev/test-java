package com.belajar.service;

import com.belajar.event.OrderCreatedEvent;
import com.belajar.model.Order;
import com.belajar.model.OrderItem;
import com.belajar.model.OrderStatus;
import com.belajar.model.Role;
import com.belajar.repository.OrderRepository;
import com.belajar.repository.ProductRepository;
import com.belajar.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.belajar.config.RabbitMQConfig;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RabbitTemplate rabbitTemplate;

    public record CartItemDto(Long productId, Integer quantity) {}

    public OrderService(OrderRepository orderRepository, 
                        ProductRepository productRepository, 
                        UserRepository userRepository,
                        ApplicationEventPublisher eventPublisher,
                        RabbitTemplate rabbitTemplate) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.rabbitTemplate = rabbitTemplate;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional
    public Order checkout(String buyerEmail, List<CartItemDto> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalStateException("Keranjang belanja tidak boleh kosong!");
        }

        // 1. Ambil data Pembeli dari email token JWT
        var buyer = userRepository.findByEmail(buyerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Pembeli tidak ditemukan: " + buyerEmail));

        if (buyer.getRole() != Role.BUYER) {
            throw new IllegalStateException("Hanya akun BUYER yang berhak melakukan checkout!");
        }

        Order order = new Order(buyer);

        // 2. Potong stok tiap barang
        for (var item : cartItems) {
            var product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new IllegalArgumentException("Produk ID " + item.productId() + " tidak ditemukan!"));

            if (product.getStock() < item.quantity()) {
                throw new IllegalStateException("Stok tidak mencukupi untuk: " + product.getName() 
                        + " (Sisa: " + product.getStock() + ", Diminta: " + item.quantity() + ")");
            }

            product.setStock(product.getStock() - item.quantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem(order, product.getId(), product.getName(), product.getPrice(), item.quantity());
            order.addItem(orderItem);
        }

        // 3. Simpan pesanan ke database
        Order savedOrder = orderRepository.save(order);

        // 4. Publikasikan pesan ke antrean terdistribusi RabbitMQ (Durable / Tidak akan hilang)
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, savedOrder.getId());

        // 5. Tetap publikasikan event in-memory Spring untuk listener lokal
        eventPublisher.publishEvent(new OrderCreatedEvent(savedOrder));

        return savedOrder;
    }

    @Transactional
    public Order updateStatus(Long orderId, OrderStatus newStatus) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order dengan ID " + orderId + " tidak ditemukan!"));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order yang sudah CANCELLED tidak bisa diubah statusnya lagi!");
        }

        if (newStatus == OrderStatus.CANCELLED) {
            for (var item : order.getItems()) {
                productRepository.findById(item.getProductId()).ifPresent(product -> {
                    product.setStock(product.getStock() + item.getQuantity());
                    productRepository.save(product);
                });
            }
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }
}
