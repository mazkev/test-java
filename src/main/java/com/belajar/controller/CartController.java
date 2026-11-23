package com.belajar.controller;

import com.belajar.dto.AddToCartRequest;
import com.belajar.dto.CartResponse;
import com.belajar.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // 1. GET /api/cart : Lihat isi keranjang belanja saya
    @GetMapping
    public ResponseEntity<CartResponse> getMyCart(Principal principal) {
        CartResponse cart = cartService.getMyCart(principal.getName());
        return ResponseEntity.ok(cart);
    }

    // 2. POST /api/cart/items : Masukkan barang ke keranjang
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            Principal principal) {
        CartResponse updatedCart = cartService.addToCart(request, principal.getName());
        return ResponseEntity.ok(updatedCart);
    }

    // 3. PUT /api/cart/items/{itemId} : Ubah jumlah barang di keranjang
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @PathVariable Long itemId,
            @RequestParam Integer quantity,
            Principal principal) {
        CartResponse updatedCart = cartService.updateItemQuantity(itemId, quantity, principal.getName());
        return ResponseEntity.ok(updatedCart);
    }

    // 4. DELETE /api/cart/items/{itemId} : Hapus 1 barang dari keranjang
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(
            @PathVariable Long itemId,
            Principal principal) {
        CartResponse updatedCart = cartService.removeItem(itemId, principal.getName());
        return ResponseEntity.ok(updatedCart);
    }

    // 5. DELETE /api/cart : Kosongkan seluruh isi keranjang
    @DeleteMapping
    public ResponseEntity<Void> clearCart(Principal principal) {
        cartService.clearCart(principal.getName());
        return ResponseEntity.noContent().build();
    }
}