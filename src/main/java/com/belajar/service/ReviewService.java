package com.belajar.service;

import com.belajar.dto.CreateReviewRequest;
import com.belajar.model.Product;
import com.belajar.model.Review;
import com.belajar.model.Role;
import com.belajar.model.User;
import com.belajar.repository.ProductRepository;
import com.belajar.repository.ReviewRepository;
import com.belajar.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public List<Review> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductId(productId);
    }

    @Transactional
    public Review addReview(CreateReviewRequest request, String userEmail) {
        // 1. Ambil data user yang sedang login dari tiket JWT
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User dengan email " + userEmail + " tidak ditemukan!"));

        if (user.getRole() != Role.BUYER) {
            throw new IllegalStateException("Hanya akun dengan role BUYER yang berhak memberi ulasan produk!");
        }

        // 2. Ambil produk yang diulas
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new IllegalArgumentException("Produk dengan ID " + request.productId() + " tidak ditemukan!"));

        // 3. Simpan ulasan baru
        Review review = new Review(product, user, request.rating(), request.comment());
        Review savedReview = reviewRepository.save(review);

        // 4. HITUNG ULANG RATING RATA-RATA PRODUK
        List<Review> allReviews = reviewRepository.findByProductId(product.getId());
        double averageRating = allReviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        // Bulatkan ke 1 desimal (contoh: 4.8)
        double roundedRating = Math.round(averageRating * 10.0) / 10.0;

        product.setRatingAvg(roundedRating);
        product.setReviewCount(allReviews.size());
        productRepository.save(product);

        return savedReview;
    }
}