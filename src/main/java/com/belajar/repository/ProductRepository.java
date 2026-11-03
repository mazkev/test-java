package com.belajar.repository;

import com.belajar.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // 1. Filter kategori dengan Paging
    Page<Product> findByCategory(String category, Pageable pageable);

    // 2. Pencarian nama dengan Paging
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<Product> findBySellerId(Long sellerId);
    Page<Product> findBySellerId(Long sellerId, Pageable pageable);

}
