package com.belajar.repository;

import com.belajar.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // 1. Filter berdasarkan kategori persis (misal: "Minuman")
    // SQL: SELECT * FROM products WHERE category = ?
    List<Product> findByCategory(String category);

    // 2. Cari berdasarkan potongan nama (tidak sensitif huruf besar/kecil)
    // SQL: SELECT * FROM products WHERE LOWER(name) LIKE '%?%'
    List<Product> findByNameContainingIgnoreCase(String name);

        // Cari semua barang milik toko tertentu (misal toko ID 1)
    List<Product> findBySellerId(Long sellerId);

}
