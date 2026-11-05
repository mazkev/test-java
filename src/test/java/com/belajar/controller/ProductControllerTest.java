package com.belajar.controller;

import com.belajar.exception.GlobalExceptionHandler;
import com.belajar.model.Product;
import com.belajar.repository.ProductRepository;
import com.belajar.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProductController productController;

    private Product mockProduct;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockProduct = new Product("Kopi Sidikalang", "Kopi Robusta Premium", 65000.0, 20, "Minuman");
        mockProduct.setId(1L);
    }

    @Test
    @DisplayName("GET /api/products/1: Mengembalikan 200 OK dan detail produk tunggal")
    void getProductById_Success() throws Exception {
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Kopi Sidikalang"))
                .andExpect(jsonPath("$.price").value(65000.0))
                .andExpect(jsonPath("$.stock").value(20));
    }

    @Test
    @DisplayName("GET /api/products/999: Mengembalikan 404 Not Found jika produk tidak ada")
    void getProductById_NotFound() throws Exception {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Produk dengan ID 999 tidak ditemukan!"));
    }

    @Test
    @DisplayName("POST /api/products: Ditolak dengan 400 Bad Request jika input kosong")
    void createProduct_FailsValidation_WhenEmptyInput() throws Exception {
        // Request body kosong tanpa nama, harga, stok, dsb.
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }
}
