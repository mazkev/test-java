package com.belajar.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateProductRequest(
    @NotBlank(message = "Nama produk tidak boleh kosong!")
    String name,

    @NotNull(message = "Harga produk wajib diisi!")
    @Positive(message = "Harga produk harus lebih besar dari 0!")
    Double price,

    @NotNull(message = "Stok produk wajib diisi!")
    @Min(value = 0, message = "Stok produk tidak boleh bernilai negatif!")
    Integer stock,

    @NotNull(message = "ID Penjual (sellerId) wajib diisi!")
    Long sellerId
) {
    // Dengan record: otomatis aman, tidak bisa disisipi kolom liar oleh hacker!
}