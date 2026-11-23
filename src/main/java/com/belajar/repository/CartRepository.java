package com.belajar.repository;

import com.belajar.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    // Mencari keranjang belanja milik user berdasarkan email
    Optional<Cart> findByUserEmail(String email);
}