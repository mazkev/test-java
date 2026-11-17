package com.belajar.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CheckoutRequest(

    @NotEmpty(message = "Keranjang belanja tidak boleh kosong!")
    List<CartItemDto> items,

    // Field tambahan transaksi real:
    String shippingAddress,
    String shippingCourier,
    Double shippingCost,
    String paymentMethod
) {
    public record CartItemDto(
        @NotNull(message = "ID Produk wajib diisi!")
        Long productId,

        @NotNull(message = "Jumlah beli wajib diisi!")
        Integer quantity
    ) {}
}