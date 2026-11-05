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

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

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

    @InjectMocks
    private OrderService orderService;

    private User buyer;
    private Product coffeeProduct;

    @BeforeEach
    void setUp() {
        buyer = new User("Budi Pembeli", "budi@mail.com", "rahasia123", Role.BUYER);
        coffeeProduct = new Product("Kopi Gayo", "Kopi Arabika 250g", 85000.0, 10, "Minuman");
        coffeeProduct.setId(1L);
    }

    @Test
    @DisplayName("Checkout Sukses: Stok berkurang dan pesan RabbitMQ terkirim")
    void checkout_Success_DeductsStockAndPublishesMessage() {
        // Arrange
        when(userRepository.findByEmail("budi@mail.com")).thenReturn(Optional.of(buyer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(coffeeProduct));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(100L);
            return o;
        });

        var cartItems = List.of(new OrderService.CartItemDto(1L, 2));

        // Act
        Order createdOrder = orderService.checkout("budi@mail.com", cartItems);

        // Assert
        assertThat(createdOrder).isNotNull();
        assertThat(createdOrder.getId()).isEqualTo(100L);
        assertThat(createdOrder.getTotalAmount()).isEqualTo(170000.0);
        assertThat(coffeeProduct.getStock()).isEqualTo(8); // 10 - 2

        // Verifikasi repository menyimpan produk baru dengan stok 8
        verify(productRepository, times(1)).save(coffeeProduct);
        verify(orderRepository, times(1)).save(any(Order.class));

        // Verifikasi RabbitMQ menerima pengiriman DTO
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.ROUTING_KEY),
                any(OrderMessageDto.class)
        );
    }

    @Test
    @DisplayName("Checkout Gagal: Stok barang tidak mencukupi melempar IllegalStateException")
    void checkout_Fails_WhenInsufficientStock() {
        // Arrange: Sisa stok hanya 10, pembeli minta 15
        when(userRepository.findByEmail("budi@mail.com")).thenReturn(Optional.of(buyer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(coffeeProduct));

        var cartItems = List.of(new OrderService.CartItemDto(1L, 15));

        // Act & Assert
        assertThatThrownBy(() -> orderService.checkout("budi@mail.com", cartItems))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Stok tidak mencukupi");

        // Verifikasi tidak ada order yang disimpan dan tidak ada event yang dipublikasi
        verify(orderRepository, never()).save(any(Order.class));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    @DisplayName("Checkout Gagal: User dengan role SELLER tidak boleh checkout")
    void checkout_Fails_WhenUserIsNotBuyer() {
        // Arrange
        User seller = new User("Toko Kopi", "toko@mail.com", "pass123", Role.SELLER);
        when(userRepository.findByEmail("toko@mail.com")).thenReturn(Optional.of(seller));

        var cartItems = List.of(new OrderService.CartItemDto(1L, 1));

        // Act & Assert
        assertThatThrownBy(() -> orderService.checkout("toko@mail.com", cartItems))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Hanya akun BUYER yang berhak");
    }
}

