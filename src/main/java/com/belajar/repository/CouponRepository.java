package com.belajar.repository;

import com.belajar.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    // Mencari kupon berdasarkan kodenya (case-insensitive, misal: diskon10 atau DISKON10)
    Optional<Coupon> findByCodeIgnoreCase(String code);
}