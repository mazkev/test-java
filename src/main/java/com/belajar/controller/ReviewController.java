package com.belajar.controller;

import com.belajar.dto.CreateReviewRequest;
import com.belajar.model.Review;
import com.belajar.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // 1. Publik: Lihat semua ulasan pada produk tertentu
    @GetMapping("/product/{productId}")
    public List<Review> getReviewsByProduct(@PathVariable Long productId) {
        return reviewService.getReviewsByProduct(productId);
    }

    // 2. Pembeli (BUYER): Kirim ulasan dan rating bintang (1-5)
    @PostMapping
    public ResponseEntity<Review> createReview(
            @Valid @RequestBody CreateReviewRequest request,
            Principal principal) {
        Review savedReview = reviewService.addReview(request, principal.getName());
        return new ResponseEntity<>(savedReview, HttpStatus.CREATED);
    }
}