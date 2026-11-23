package com.belajar.dto;

import java.util.List;

public record CartResponse(
    Long cartId,
    Integer totalItems,
    Double totalPrice,
    List<CartItemDetail> items
) {
    public record CartItemDetail(
        Long itemId,
        Long productId,
        String productName,
        Double productPrice,
        Integer quantity,
        Double subtotal
    ) {}
}