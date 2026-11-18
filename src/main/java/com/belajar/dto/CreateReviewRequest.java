package com.belajar.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateReviewRequest(
    @NotNull(message = "ID Produk wajib diisi!")
    Long productId,

    @NotNull(message = "Rating wajib diisi!")
    @Min(value = 1, message = "Rating minimal bintang 1!")
    @Max(value = 5, message = "Rating maksimal bintang 5!")
    Integer rating,

    String comment
) {}