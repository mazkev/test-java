package com.belajar.controller;

import com.belajar.model.Coupon;
import com.belajar.repository.CouponRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    private final CouponRepository couponRepository;

    public CouponController(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    // 1. Publik: Lihat semua kupon promosi yang ada
    @GetMapping
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    // 2. Cek Kupon: Cek apakah kode kupon valid untuk jumlah belanja tertentu sebelum checkout
    @GetMapping("/check")
    public ResponseEntity<?> checkCoupon(
            @RequestParam String code,
            @RequestParam Double amount) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new IllegalArgumentException("Kupon dengan kode '" + code + "' tidak ditemukan!"));

        Double discount = coupon.calculateDiscount(amount);
        boolean isValid = discount > 0.0;

        return ResponseEntity.ok(Map.of(
                "code", coupon.getCode(),
                "discountType", coupon.getDiscountType(),
                "discountValue", coupon.getDiscountValue(),
                "minPurchase", coupon.getMinPurchase(),
                "discountAmount", discount,
                "valid", isValid
        ));
    }
}