package com.belajar.service;

import com.belajar.dto.AddToCartRequest;
import com.belajar.dto.CartResponse;
import com.belajar.model.*;
import com.belajar.repository.CartRepository;
import com.belajar.repository.ProductRepository;
import com.belajar.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // Helper privat: Mengambil keranjang user, atau otomatis membuat baru jika belum punya
    private Cart getOrCreateCart(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User dengan email " + userEmail + " tidak ditemukan!"));

        if (user.getRole() != Role.BUYER) {
            throw new IllegalStateException("Hanya akun dengan role BUYER yang memiliki keranjang belanja!");
        }

        return cartRepository.findByUserEmail(userEmail)
                .orElseGet(() -> cartRepository.save(new Cart(user)));
    }

    // Helper privat: Mengubah Entity Cart menjadi CartResponse DTO yang rapi
    private CartResponse mapToResponse(Cart cart) {
        List<CartResponse.CartItemDetail> itemDetails = cart.getItems().stream()
                .map(item -> new CartResponse.CartItemDetail(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getPrice(),
                        item.getQuantity(),
                        item.getSubtotal()
                ))
                .toList();

        return new CartResponse(
                cart.getId(),
                cart.getTotalItems(),
                cart.getTotalPrice(),
                itemDetails
        );
    }

    // 1. Melihat isi keranjang belanja milik pembeli yang login
    @Transactional(readOnly = true)
    public CartResponse getMyCart(String userEmail) {
        Cart cart = getOrCreateCart(userEmail);
        return mapToResponse(cart);
    }

    // 2. Menambahkan barang ke keranjang
    @Transactional
    public CartResponse addToCart(AddToCartRequest request, String userEmail) {
        Cart cart = getOrCreateCart(userEmail);

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new IllegalArgumentException("Produk ID " + request.productId() + " tidak ditemukan!"));

        // Cek apakah produk ini sudah ada di dalam keranjang belanja
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        int targetQuantity = request.quantity();
        if (existingItem.isPresent()) {
            targetQuantity += existingItem.get().getQuantity();
        }

        // Validasi stok toko
        if (product.getStock() < targetQuantity) {
            throw new IllegalStateException("Stok toko tidak mencukupi! (Sisa stok: " 
                    + product.getStock() + ", total diminta di keranjang: " + targetQuantity + ")");
        }

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(targetQuantity);
        } else {
            CartItem newItem = new CartItem(cart, product, request.quantity());
            cart.getItems().add(newItem);
        }

        Cart saved = cartRepository.save(cart);
        return mapToResponse(saved);
    }

    // 3. Mengubah jumlah barang tertentu di keranjang
    @Transactional
    public CartResponse updateItemQuantity(Long itemId, Integer newQuantity, String userEmail) {
        Cart cart = getOrCreateCart(userEmail);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item keranjang ID " + itemId + " tidak ditemukan!"));

        if (newQuantity <= 0) {
            cart.getItems().remove(item);
        } else {
            if (item.getProduct().getStock() < newQuantity) {
                throw new IllegalStateException("Stok tidak mencukupi! Sisa stok: " + item.getProduct().getStock());
            }
            item.setQuantity(newQuantity);
        }

        Cart saved = cartRepository.save(cart);
        return mapToResponse(saved);
    }

    // 4. Menghapus 1 barang dari keranjang
    @Transactional
    public CartResponse removeItem(Long itemId, String userEmail) {
        Cart cart = getOrCreateCart(userEmail);

        boolean removed = cart.getItems().removeIf(item -> item.getId().equals(itemId));
        if (!removed) {
            throw new IllegalArgumentException("Item keranjang ID " + itemId + " tidak ditemukan!");
        }

        Cart saved = cartRepository.save(cart);
        return mapToResponse(saved);
    }

    // 5. Mengosongkan keranjang belanja
    @Transactional
    public void clearCart(String userEmail) {
        Cart cart = getOrCreateCart(userEmail);
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}