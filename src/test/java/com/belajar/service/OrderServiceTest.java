package com.belajar.service;

import com.belajar.config.RabbitMQConfig;
import com.belajar.event.OrderMessageDto;
import com.belajar.model.Order;
import com.belajar.model.Product;
import com.belajar.model.Role;
import com.belajar.model.User;
import com.belajar.repository.OrderRepository;
import com.belajar.repository.ProductRepository;
import com.belajar.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// 1. @ExtendWith(MockitoExtension.class) mengaktifkan fitur Mockito di JUnit 5
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    // 2. @Mock membuat objek tiruan (palsu) tanpa menyentuh Database SQL asli atau RabbitMQ
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private RabbitTemplate rabbitTemplate;

    // 3. @InjectMocks membuat instance asli OrderService dan otomatis menyuntikkan semua @Mock di atas ke dalamnya
    @InjectMocks
    private OrderService orderService;

    private User buyer;
    private Product coffeeProduct;

    // 4. @BeforeEach dijalankan ulang sebelum setiap unit test untuk mereset data awal
    @BeforeEach
    void setUp() {
        buyer = new User("Budi Pembeli", "budi@mail.com", "rahasia123", Role.BUYER);
        
        // Produk awal: Kopi Gayo, stok = 10
        coffeeProduct = new Product("Kopi Gayo", "Kopi Arabika 250g", 85000.0, 10, "Minuman");
        coffeeProduct.setId(1L);
    }

    @Test
    @DisplayName("Skenario 1: Checkout Sukses - Stok terpotong dan pesan dikirim ke RabbitMQ")
    void checkout_Success_DeductsStockAndPublishesMessage() {
        // GIVEN (Persiapan Data & Perilaku Mock)
        when(userRepository.findByEmail("budi@mail.com")).thenReturn(Optional.of(buyer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(coffeeProduct));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(100L); // Simulasi ID terbit dari database
            return o;
        });

        var cartItems = List.of(new OrderService.CartItemDto(1L, 2)); // Beli 2 item

        // WHEN (Eksekusi Method Bisnis yang Diuji)
        Order createdOrder = orderService.checkout("budi@mail.com", cartItems);

        // THEN (Verifikasi Hasil dengan AssertJ)
        assertThat(createdOrder).isNotNull();
        assertThat(createdOrder.getId()).isEqualTo(100L);
        assertThat(createdOrder.getTotalAmount()).isEqualTo(170000.0); // 85000 * 2
        assertThat(coffeeProduct.getStock()).isEqualTo(8); // 10 dikurang 2 sisa 8!

        // Verifikasi bahwa database benar-benar dipanggil untuk menyimpan perubahan
        verify(productRepository, times(1)).save(coffeeProduct);
        verify(orderRepository, times(1)).save(any(Order.class));

        // Verifikasi bahwa RabbitMQ benar-benar menerima payload DTO
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.ROUTING_KEY),
                any(OrderMessageDto.class)
        );
    }

    @Test
    @DisplayName("Skenario 2: Checkout Gagal - Stok tidak mencukupi melempar IllegalStateException")
    void checkout_Fails_WhenInsufficientStock() {
        // GIVEN: Stok hanya 10, tapi pembeli meminta 15
        when(userRepository.findByEmail("budi@mail.com")).thenReturn(Optional.of(buyer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(coffeeProduct));

        var cartItems = List.of(new OrderService.CartItemDto(1L, 15));

        // WHEN & THEN: Harus melempar IllegalStateException dengan pesan error yang spesifik
        assertThatThrownBy(() -> orderService.checkout("budi@mail.com", cartItems))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Stok tidak mencukupi");

        // VERIFIKASI KEAMANAN: Pastikan pesanan TIDAK tersimpan dan RabbitMQ TIDAK dipanggil!
        verify(orderRepository, never()).save(any(Order.class));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    @DisplayName("Skenario 3: Checkout Gagal - User dengan role SELLER dilarang belanja")
    void checkout_Fails_WhenUserIsNotBuyer() {
        // GIVEN: User ber-role SELLER
        User seller = new User("Toko Kopi", "toko@mail.com", "pass123", Role.SELLER);
        when(userRepository.findByEmail("toko@mail.com")).thenReturn(Optional.of(seller));

        var cartItems = List.of(new OrderService.CartItemDto(1L, 1));

        // WHEN & THEN
        assertThatThrownBy(() -> orderService.checkout("toko@mail.com", cartItems))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Hanya akun BUYER yang berhak");
    }
}