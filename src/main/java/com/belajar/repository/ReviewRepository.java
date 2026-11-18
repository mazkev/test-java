package com.belajar.repository;

import com.belajar.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Mencari semua ulasan untuk produk tertentu
    List<Review> findByProductId(Long productId);
}