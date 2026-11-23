package com.belajar.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddToCartRequest(
    @NotNull(message = "ID Produk wajib diisi!")
    Long productId,

    @NotNull(message = "Jumlah barang wajib diisi!")
    @Min(value = 1, message = "Minimal jumlah beli adalah 1 barang!")
    Integer quantity
) {}