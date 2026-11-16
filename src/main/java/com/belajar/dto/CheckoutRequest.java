package com.belajar.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CheckoutRequest(
    @NotNull(message = "ID Pembeli (buyerId) wajib diisi!")
    Long buyerId,

    @NotEmpty(message = "Keranjang belanja tidak boleh kosong!")
    List<CartItemDto> items
) {
    // Record kecil untuk tiap item di keranjang belanja
    public record CartItemDto(
        @NotNull(message = "ID Produk wajib diisi!")
        Long productId,

        @NotNull(message = "Jumlah beli wajib diisi!")
        Integer quantity
    ) {}
}