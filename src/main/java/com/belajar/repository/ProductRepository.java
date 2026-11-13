package com.belajar.repository;

import com.belajar.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Otomatis mewarisi findAll(), findById(), save(), deleteById()
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    // 2. Filter berdasarkan Kategori persis
    Page<Product> findByCategoryIgnoreCase(String category, Pageable pageable);
    // 3. Cari berdasarkan Nama DAN Kategori sekaligus
    Page<Product> findByNameContainingIgnoreCaseAndCategoryIgnoreCase(String name, String category, Pageable pageable);
}